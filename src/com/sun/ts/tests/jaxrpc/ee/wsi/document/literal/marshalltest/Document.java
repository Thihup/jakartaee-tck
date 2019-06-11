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

// This class was generated by the JAXRPC RI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Reference Implementation (1.1, build EA-R17)

package com.sun.ts.tests.jaxrpc.ee.wsi.document.literal.marshalltest;

public class Document {
  protected java.lang.String ID;

  protected java.lang.String _value;

  public Document() {
  }

  public Document(java.lang.String ID, java.lang.String _value) {
    this.ID = ID;
    this._value = _value;
  }

  public java.lang.String getID() {
    return ID;
  }

  public void setID(java.lang.String ID) {
    this.ID = ID;
  }

  public java.lang.String get_value() {
    return _value;
  }

  public void set_value(java.lang.String _value) {
    this._value = _value;
  }
}