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

package com.sun.ts.tests.jaxws.wsi.j2w.rpc.literal.R2303;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.javatest.Status;
import com.sun.ts.lib.harness.ServiceEETest;
import com.sun.ts.tests.jaxws.sharedclients.ClientFactory;
import com.sun.ts.tests.jaxws.sharedclients.SOAPClient;
import com.sun.ts.tests.jaxws.sharedclients.rpclitclient.J2WRLSharedClient;
import com.sun.ts.tests.jaxws.wsi.constants.DescriptionConstants;
import com.sun.ts.tests.jaxws.wsi.utils.DescriptionUtils;

public class Client extends ServiceEETest implements DescriptionConstants {
  /**
   * The client.
   */
  private SOAPClient client;

  static J2WRLShared service = null;

  /**
   * Test entry point.
   * 
   * @param args
   *          the command-line arguments.
   */
  public static void main(String[] args) {
    Client test = new Client();
    Status status = test.run(args, System.out, System.err);
    status.exit();
  }

  /**
   * @class.testArgs: -ap jaxws-url-props.dat
   * @class.setup_props: webServerHost; webServerPort; platform.mode;
   *
   * @param args
   * @param properties
   *
   * @throws Fault
   */
  public void setup(String[] args, Properties properties) throws Fault {
    client = ClientFactory.getClient(J2WRLSharedClient.class, properties, this,
        service);
    logMsg("setup ok");
  }

  public void cleanup() {
    logMsg("cleanup");
  }

  /**
   * @testName: testOperations
   *
   * @assertion_ids: WSI:SPEC:R2303
   *
   * @test_Strategy: Retrieve the WSDL, generated by the Java-to-WSDL tool, and
   *                 examine the operation(s) making sure they are not
   *                 Notification or Solicit-Response kinds.
   *
   * @throws Fault
   */
  public void testOperations() throws Fault {
    Document document = client.getDocument();
    Element[] portTypes = DescriptionUtils.getPortTypes(document);
    for (int i = 0; i < portTypes.length; i++) {
      verifyPortType(portTypes[i]);
    }
  }

  protected void verifyPortType(Element element) throws Fault {
    Element[] children = DescriptionUtils.getChildElements(element,
        WSDL_NAMESPACE_URI, WSDL_OPERATION_LOCAL_NAME);
    for (int i = 0; i < children.length; i++) {
      verifyOperation(children[i]);
    }
  }

  protected void verifyOperation(Element element) throws Fault {
    String name = element.getAttribute(WSDL_NAME_ATTR);
    int input = -1;
    int output = -1;
    int fault = -1;
    Element[] children = DescriptionUtils.getChildElements(element);
    for (int i = 0; i < children.length; i++) {
      String namespaceURI = children[i].getNamespaceURI();
      if (!WSDL_NAMESPACE_URI.equals(namespaceURI)) {
        continue;
      }
      String localName = children[i].getLocalName();
      if (WSDL_FAULT_LOCAL_NAME.equals(localName)) {
        if (fault != -1) {
          throw new Fault("More than one wsdl:fault encountered in operation '"
              + name + "' (BP-R2303)");
        }
        fault = i;
      } else if (WSDL_INPUT_LOCAL_NAME.equals(localName)) {
        if (input != -1) {
          throw new Fault("More than one wsdl:input encountered in operation '"
              + name + "' (BP-R2303)");
        }
        input = i;
      } else if (WSDL_OUTPUT_LOCAL_NAME.equals(localName)) {
        if (output != -1) {
          throw new Fault("More than one wsdl:output encountered in operation '"
              + name + "' (BP-R2303)");
        }
        output = i;
      }
    }
    if ((input == 0) && (output == -1)) {
      // ONE WAY
    } else if ((input == 0) && (output == 1)) {
      // REQUEST/RESPONSE
    } else if ((output == 0) && (input == 1)) {
      // SOLICIT RESPONSE
      throw new Fault(
          "'Solicit Response' operation '" + name + "' encountered (BP-R2303)");
    } else if ((output == 0) && (input == -1)) {
      // NOTIFICATION
      throw new Fault(
          "'Notification' operation '" + name + "' encountered (BP-R2303)");
    } else {
      throw new Fault("Operation '" + name + "' has unknown type (BP-R2303)");
    }
  }
}
