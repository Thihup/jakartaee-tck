/*
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ts.tests.assembly.classpath.util;

import com.sun.ts.lib.util.TestUtil;

/**
 * This class is useless on its own. Its only interest is to be defined in a
 * package that contains no other class.
 *
 * It is used by some TS tests that check that this package is in their
 * classpath by trying to create an instance of ClassPathUtil.
 */
public class ClassPathUtil {

  /** No args constructor */
  public ClassPathUtil() {
  }

  /** Check current instance is valid */
  public void testDirectLibrary() {
    TestUtil.logTrace("[ClassPathUtil] testDirectLibrary()");
  }

  /** To check we can get a valid instance of IndirectClassPathUtil */
  public void testIndirectLibrary() {
    IndirectClassPathUtil ref;

    TestUtil.logTrace("[ClassPathUtil] testIndirectLibrary()");
    ref = new IndirectClassPathUtil();
    ref.test();
  }

}
