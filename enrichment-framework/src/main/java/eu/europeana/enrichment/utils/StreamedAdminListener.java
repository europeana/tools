/*
 * Copyright 2005-2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.europeana.enrichment.utils;

/**
 * A Sesame listener suitable for diverting into a (File) Stream.
 * 
 * @author Borys Omelayenko
 * 
 */
public class StreamedAdminListener
{
	/*
	 * implements AdminListener {
	 * 
	 * private PrintStream out = System.out; // Start/end transaction public void
	 * transactionStart() { out.println("Transaction started."); }
	 * 
	 * public void transactionEnd() { out.println("Transaction finished."); }
	 * 
	 * // Status. msg may be null. public void status( String msg, int lineNo, int
	 * colNo) { out.println("[status ] " + _createMessage(msg, lineNo, colNo,
	 * null)); }
	 * 
	 * // Notification. msg may be null. public void notification( String msg, int
	 * lineNo, int colNo, Statement statement) { out.println("[notify ] " +
	 * _createMessage(msg, lineNo, colNo, statement)); }
	 * 
	 * // Warning. msg may be null. public void warning( String msg, int lineNo,
	 * int colNo, Statement statement) { out.println("[WARNING] " +
	 * _createMessage(msg, lineNo, colNo, statement)); }
	 * 
	 * // Error. msg may be null. public void error( String msg, int lineNo, int
	 * colNo, Statement statement) { out.println("[ERROR  ] " +
	 * _createMessage(msg, lineNo, colNo, statement)); }
	 * 
	 * private String _createMessage( String msg, int lineNo, int colNo, Statement
	 * statement) { StringBuffer result = new StringBuffer();
	 * 
	 * if (lineNo != -1) { result.append("("); result.append(lineNo);
	 * result.append(", "); result.append(colNo); result.append(")"); }
	 * 
	 * result.append(": "); result.append(msg);
	 * 
	 * if (statement != null) { result.append("\n  -> subject  : " +
	 * statement.getSubject()); result.append("\n  -> predicate: " +
	 * statement.getPredicate()); result.append("\n  -> object   : " +
	 * statement.getObject()); }
	 * 
	 * return result.toString(); }
	 * 
	 * public StreamedAdminListener(PrintStream out) { super(); this.out = out; }
	 * 
	 * public StreamedAdminListener() { super(); }
	 */
}
