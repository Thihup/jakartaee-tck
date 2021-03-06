/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

/*
 * $Id$
 */

package com.sun.ts.tests.ejb.ee.tx.entity.pm.bm.Tx_Multi;

import java.util.Properties;

import com.sun.javatest.Status;
import com.sun.ts.lib.harness.EETest;
import com.sun.ts.lib.util.TSNamingContext;
import com.sun.ts.lib.util.TestUtil;
import com.sun.ts.tests.ejb.ee.tx.txEPMbean.TxEPMBean;
import com.sun.ts.tests.ejb.ee.tx.txEPMbean.TxEPMBeanHome;

public class Client extends EETest {

  private static final int SLEEPTIME = 5000;

  private static final String testName = "Tx_Multi";

  private static final String tLookup = "java:comp/env/ejb/TestBean";

  private static final String tLookupER = "java:comp/env/ejb/TxRequired";

  private static final String tLookupERN = "java:comp/env/ejb/TxRequiresNew";

  private static Properties props = null;

  private static TSNamingContext nctx = null;

  private String tName = null;

  private TestBeanHome s1Home = null;

  private TestBeanHome s2Home = null;

  private TxEPMBeanHome eHomeR = null;

  private TxEPMBeanHome eHomeRN = null;

  private TestBean beanRefS1 = null;

  private TestBean beanRefS2 = null;

  private TxEPMBean beanRefER = null;

  private TxEPMBean beanRefERN = null;

  private Integer pkeyR = null;

  private Integer pkeyRN = null;

  // For thread synchronization
  private static final int NUMLOOPS = 3;

  private static final int NTHREADS = 2;

  private static Object lock = new Object();

  private static Object startLock = new Object();

  private static Object workLock = new Object();

  private static int threadsDone = 0;

  private static int errors = 0;

  // Inner class TestThread
  class TestThread implements Runnable {
    // Instance variables
    private int threadNum = 0;

    private int testNum = 0;

    private boolean synchronize = true;

    // n represents the number of threads
    // t is the test case number id
    public TestThread(int n, int t) {
      this.threadNum = n;
      this.testNum = t;
    }

    public void run() {
      synchronized (lock) {
        ++threadsDone;
        lock.notifyAll();
      }

      synchronized (startLock) {
        try {
          startLock.wait();
        } catch (InterruptedException ignore) {
        }
      }

      boolean testResult = false;

      if (testNum == 1)
        testResult = runTest1();
      else if (testNum == 2)
        testResult = runTest2();
      else if (testNum == 3)
        testResult = runTest3();
      else if (testNum == 4)
        testResult = runTest4();

      synchronized (lock) {
        if (!testResult)
          ++errors;
        ++threadsDone;
        lock.notifyAll();
      }
    }
  }

  /* Test setup */

  /*
   * @class.setup_props: java.naming.factory.initial;
   * 
   * @class.testArgs: -ap tssql.stmt
   *
   */
  public void setup(String[] args, Properties p) throws Fault {
    logMsg("setup");
    props = p;

    try {
      nctx = new TSNamingContext();

      // Lookup table name
      tName = TestUtil.getTableName(TestUtil.getProperty("TxEBean_Delete"));
      logMsg("Looking up table name " + tName);

      // Get the first Session EJB Home and create an instance
      logMsg("Looking up home interface for EJB: " + tLookup);
      s1Home = (TestBeanHome) nctx.lookup(tLookup, TestBeanHome.class);
      logMsg("Creating Session  EJB");
      beanRefS1 = (TestBean) s1Home.create(props);

      // Get the second Session EJB Home and create an instance
      logMsg("Looking up home interface for EJB: " + tLookup);
      s2Home = (TestBeanHome) nctx.lookup(tLookup, TestBeanHome.class);
      logMsg("Creating Session S2 EJB");
      beanRefS2 = (TestBean) s2Home.create(props);

      // Get the Entity EJB Home and create an instance
      logMsg("Looking up home interface for EJB: " + tLookupER);
      eHomeR = (TxEPMBeanHome) nctx.lookup(tLookupER, TxEPMBeanHome.class);
      pkeyR = new Integer(9);
      logMsg("Creating entity EJB = " + pkeyR.toString());
      beanRefER = (TxEPMBean) eHomeR.create(tName, pkeyR,
          tName + "-" + pkeyR.intValue(), (float) 9.00, props);

      eHomeRN = (TxEPMBeanHome) nctx.lookup(tLookupERN, TxEPMBeanHome.class);
      pkeyRN = new Integer(11);
      logMsg("Creating entity EJB = " + pkeyRN.toString());
      beanRefERN = (TxEPMBean) eHomeRN.create(tName, pkeyRN,
          tName + "-" + pkeyRN.intValue(), (float) 11.00, props);

      logMsg("Setup ok");
    } catch (Exception e) {
      throw new Fault("Setup failed:", e);
    }
  }

  public boolean runTest1() {
    logMsg("runTest1");
    boolean b1, b2, testResult;
    b1 = b2 = testResult = false;

    try {
      logMsg("Synchronize calling of Session EJB methods");
      for (int i = 0; i < NUMLOOPS; i++) {
        synchronized (workLock) {
          TestUtil.logTrace("pkeyR: " + pkeyR + " tName: " + tName);
          TestUtil.logTrace("props: " + props.toString());
          b1 = beanRefS1.doTest1(pkeyR, tName, i + 1);
          b2 = beanRefS2.doTest1(pkeyR, tName, i + 2);
        }
      }
      if (b1 && b2)
        testResult = true;

    } catch (Exception e) {
      TestUtil.logErr("Exception: " + e.getMessage(), e);
      testResult = false;
    }
    logMsg("Leaving runTest1 Method .....");
    return testResult;
  }

  public boolean runTest2() {
    logMsg("runTest2");
    boolean b1, b2, testResult;
    b1 = b2 = testResult = false;

    try {
      logMsg("Synchronize calling of Session EJB methods");
      for (int i = 0; i < NUMLOOPS; i++) {
        synchronized (workLock) {
          TestUtil.logTrace("pkeyR: " + pkeyR + " tName: " + tName);
          TestUtil.logTrace("props: " + props.toString());
          b1 = beanRefS1.doTest2(pkeyR, tName, i + 1);
          b2 = beanRefS2.doTest2(pkeyR, tName, i + 2);
        }
      }
      if (b1 && b2)
        testResult = true;

    } catch (Exception e) {
      TestUtil.logErr("Exception: " + e.getMessage(), e);
      testResult = false;
    }
    logMsg("Leaving runTest2 Method .....");
    return testResult;
  }

  public boolean runTest3() {
    logMsg("runTest3");
    boolean b1, b2, testResult;
    b1 = b2 = testResult = false;

    try {
      logMsg("Synchronize calling of Session EJB methods");
      for (int i = 0; i < NUMLOOPS; i++) {
        synchronized (workLock) {
          TestUtil.logTrace("pkeyRN: " + pkeyRN + " tName: " + tName);
          TestUtil.logTrace("props: " + props.toString());
          b1 = beanRefS1.doTest3(pkeyRN, tName, i + 1);
          b2 = beanRefS2.doTest3(pkeyRN, tName, i + 2);
        }
      }
      if (b1 && b2)
        testResult = true;

    } catch (Exception e) {
      TestUtil.logErr("Exception: " + e.getMessage(), e);
      testResult = false;
    }
    logMsg("Leaving runTest3 Method .....");
    return testResult;
  }

  public boolean runTest4() {
    logMsg("runTest4");
    boolean b1, b2, testResult;
    b1 = b2 = testResult = false;

    try {
      logMsg("Synchronize calling of Session EJB methods");
      for (int i = 0; i < NUMLOOPS; i++) {
        synchronized (workLock) {
          TestUtil.logTrace("pkeyRN: " + pkeyRN + " tName: " + tName);
          TestUtil.logTrace("props: " + props.toString());
          b1 = beanRefS1.doTest4(pkeyRN, tName, i + 1);
          b2 = beanRefS2.doTest4(pkeyRN, tName, i + 2);
        }
      }
      if (b1 && b2)
        testResult = true;

    } catch (Exception e) {
      TestUtil.logErr("Exception: " + e.getMessage(), e);
      testResult = false;
    }
    logMsg("Leaving runTest4 Method .....");
    return testResult;
  }

  public static void main(String[] args) {
    Client theTests = new Client();
    Status s = theTests.run(args, System.out, System.err);
    s.exit();
  }

  /* Run test */

  /*
   * @testName: test1
   *
   * @assertion_ids: EJB:SPEC:10122; EJB:SPEC:583.1; EJB:SPEC:583.2
   *
   * @test_Strategy: Multiple Bean Managed Tx commit - Required Entity EJBs
   * Create a Required Entity EJB with Primary Key N. Create multiple threads.
   * Within each thread create a Session EJB. Start a new transaction within the
   * Session Bean. Within each Session EJB lookup the previously created Entity
   * bean using Primary Key N. Then update the data in the Entity bean from
   * within each session bean (Synchronously). Commit the changes. Verify that
   * the changes were made in the database and in the instance data.
   * 
   *
   */

  public void test1() throws Fault {
    logMsg("test1");
    try {
      Thread[] threads = new Thread[NTHREADS];
      threadsDone = 0;
      errors = 0;

      logMsg("Multiple Clients (Session EJBs) accessing the same entity EJB");
      logMsg("Synchronize calling of entity EJB methods");
      logMsg("Creating " + NTHREADS + " client threads ...");

      for (int i = 0; i < NTHREADS; i++) {
        threads[i] = new Thread(new TestThread(i, 1), "TestThread-" + i);
        threads[i].start();
      }

      // Wait for creation
      logMsg("Wait for thread creation ...");
      synchronized (lock) {
        while (threadsDone < threads.length) {
          lock.wait();
        }
        try {
          Thread.sleep(SLEEPTIME);
        } catch (Exception e) {
        }
      }

      // Notify all to start
      logMsg("Notify all threads to start ...");
      synchronized (startLock) {
        threadsDone = 0;
        startLock.notifyAll();
      }

      // Wait for completion
      logMsg("Wait for all threads to finish ...");
      synchronized (lock) {
        while (threadsDone < threads.length) {
          lock.wait();
        }
      }

      if (errors > 0) {
        TestUtil.logErr("The number of errors were: " + errors);
        throw new Fault("test1 failed");
      }

    } catch (Exception e) {
      throw new Fault("test1 failed", e);
    }
  }

  /*
   * @testName: test2
   *
   * @assertion_ids: EJB:SPEC:10122; EJB:SPEC:583.1; EJB:SPEC:583.2
   *
   * @test_Strategy: Multiple Bean Managed Tx commit - Required Entity EJBs
   * Create a Required Entity EJB with Primary Key N. Create multiple threads.
   * Within each thread create a Session EJB. Start a new transaction within the
   * Session Bean. Within each Session EJB lookup the previously created Entity
   * bean using Primary Key N. Then update the data in the Entity bean from
   * within each session bean (Synchronously). Rollback the changes. Verify that
   * the changes were not made in the database and in the instance data.
   * 
   *
   */

  public void test2() throws Fault {
    logMsg("test2");
    try {
      Thread[] threads = new Thread[NTHREADS];
      threadsDone = 0;
      errors = 0;

      logMsg("Multiple Clients (Session EJBs) accessing the same entity EJB");
      logMsg("Synchronize calling of entity EJB methods");
      logMsg("Creating " + NTHREADS + " client threads ...");

      for (int i = 0; i < NTHREADS; i++) {
        threads[i] = new Thread(new TestThread(i, 2), "TestThread-" + i);
        threads[i].start();
      }

      // Wait for creation
      logMsg("Wait for thread creation ...");
      synchronized (lock) {
        while (threadsDone < threads.length) {
          lock.wait();
        }
        try {
          Thread.sleep(SLEEPTIME);
        } catch (Exception e) {
        }
      }

      // Notify all to start
      logMsg("Notify all threads to start ...");
      synchronized (startLock) {
        threadsDone = 0;
        startLock.notifyAll();
      }

      // Wait for completion
      logMsg("Wait for all threads to finish ...");
      synchronized (lock) {
        while (threadsDone < threads.length) {
          lock.wait();
        }
      }

      if (errors > 0) {
        TestUtil.logErr("The number of errors were: " + errors);
        throw new Fault("test2 failed");
      }

    } catch (Exception e) {
      throw new Fault("test2 failed", e);
    }
  }

  /*
   * @testName: test3
   *
   * @assertion_ids: EJB:SPEC:10122
   *
   * @test_Strategy: Multiple Bean Managed Tx commit - RequiresNew Entity EJBs
   * Create a RequiresNew Entity EJB with Primary Key N. Create multiple
   * threads. Within each thread create a Session EJB. Start a new transaction
   * within the Session Bean. Within each Session EJB lookup the previously
   * created Entity bean using Primary Key N. Then update the data in the Entity
   * bean from within each session bean (Synchronously). Commit the transaction.
   * Verify that the changes were made in the database and in the instance data.
   * 
   *
   */

  public void test3() throws Fault {
    logMsg("test3");
    try {
      Thread[] threads = new Thread[NTHREADS];
      threadsDone = 0;
      errors = 0;

      logMsg("Multiple Clients (Session EJBs) accessing the same entity EJB");
      logMsg("Synchronize calling of entity EJB methods");
      logMsg("Creating " + NTHREADS + " client threads ...");

      for (int i = 0; i < NTHREADS; i++) {
        threads[i] = new Thread(new TestThread(i, 3), "TestThread-" + i);
        threads[i].start();
      }

      // Wait for creation
      logMsg("Wait for thread creation ...");
      synchronized (lock) {
        while (threadsDone < threads.length) {
          lock.wait();
        }
        try {
          Thread.sleep(SLEEPTIME);
        } catch (Exception e) {
        }
      }

      // Notify all to start
      logMsg("Notify all threads to start ...");
      synchronized (startLock) {
        threadsDone = 0;
        startLock.notifyAll();
      }

      // Wait for completion
      logMsg("Wait for all threads to finish ...");
      synchronized (lock) {
        while (threadsDone < threads.length) {
          lock.wait();
        }
      }

      if (errors > 0) {
        TestUtil.logErr("The number of errors were: " + errors);
        throw new Fault("test3 failed");
      }

    } catch (Exception e) {
      throw new Fault("test3 failed", e);
    }
  }

  /*
   * @testName: test4
   *
   * @assertion_ids: EJB:SPEC:10122
   *
   * @test_Strategy: Multiple Bean Managed Tx rollback - RequiresNew Entity EJBs
   * Create a RequiresNew Entity EJB with Primary Key N. Create multiple
   * threads. Within each thread create a Session EJB. Within each Session EJB
   * lookup the previously Start a new transaction within the Session Bean.
   * created Entity bean using Primary Key N. Then update the data in the Entity
   * bean from within each session bean (Synchronously). Rollback the changes.
   * Verify that the changes were made in the database and in the instance data.
   * 
   *
   */

  public void test4() throws Fault {
    logMsg("test4");
    try {
      Thread[] threads = new Thread[NTHREADS];
      threadsDone = 0;
      errors = 0;

      logMsg("Multiple Clients (Session EJBs) accessing the same entity EJB");
      logMsg("Synchronize calling of entity EJB methods");
      logMsg("Creating " + NTHREADS + " client threads ...");

      for (int i = 0; i < NTHREADS; i++) {
        threads[i] = new Thread(new TestThread(i, 4), "TestThread-" + i);
        threads[i].start();
      }

      // Wait for creation
      logMsg("Wait for thread creation ...");
      synchronized (lock) {
        while (threadsDone < threads.length) {
          lock.wait();
        }
        try {
          Thread.sleep(SLEEPTIME);
        } catch (Exception e) {
        }
      }

      // Notify all to start
      logMsg("Notify all threads to start ...");
      synchronized (startLock) {
        threadsDone = 0;
        startLock.notifyAll();
      }

      // Wait for completion
      logMsg("Wait for all threads to finish ...");
      synchronized (lock) {
        while (threadsDone < threads.length) {
          lock.wait();
        }
      }

      if (errors > 0) {
        TestUtil.logErr("The number of errors were: " + errors);
        throw new Fault("test4 failed");
      }

    } catch (Exception e) {
      throw new Fault("test4 failed", e);
    }
  }

  public void cleanup() throws Fault {
    logMsg("cleanup");
    try {
      if (beanRefS1 != null) {
        beanRefS1.remove();
      }
      if (beanRefS2 != null) {
        beanRefS2.remove();
      }
      if (beanRefER != null) {
        beanRefER.remove();
      }
      if (beanRefERN != null) {
        beanRefERN.remove();
      }
      logMsg("cleanup ok");
    } catch (Exception e) {
      TestUtil.logErr("Exception removing EJBs" + e.getMessage(), e);
    }
  }
}
