/*
 * Copyright Siemens AG, 2013-2016. Part of the SW360 Portal Project.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License Version 2.0 as published by the
 * Free Software Foundation with classpath exception.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License version 2.0 for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (please see the COPYING file); if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */
package com.siemens.sw360.licenses;

import com.siemens.sw360.datahandler.TestUtils;
import com.siemens.sw360.datahandler.common.DatabaseSettings;
import com.siemens.sw360.datahandler.couchdb.DatabaseConnector;
import com.siemens.sw360.datahandler.thrift.SW360Exception;
import com.siemens.sw360.datahandler.thrift.licenses.*;
import com.siemens.sw360.datahandler.thrift.users.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Test class for the license service
 *
 * @author cedric.bodet@tngtech.com
 */
public class LicenseHandlerTest {

    private static final String url = DatabaseSettings.COUCH_DB_URL;
    private static final String dbName = DatabaseSettings.COUCH_DB_DATABASE;

    private LicenseHandler handler;
    private User user;

    private Map<String, License> licenses;
    private Map<String, Todo> todos;
    private Map<String, Obligation> obligations;

    public static Todo getById(String id, Collection<Todo> todos) {
        if (id != null && todos != null) {
            for (Todo todo : todos) {
                if (id.equals(todo.getId()))
                    return todo;
            }
        }
        return null;
    }

    @Before
    public void setUp() throws Exception {
        // Create the database
        TestUtils.createDatabase(url, dbName);

        // Create all test entries
        createTestEntries();

        // Create the handler
        handler = new LicenseHandler();

        // Create the user
        user = new User().setEmail("test@siemens.com").setDepartment("CT BE OP SWI OSS");
    }

    @After
    public void tearDown() throws Exception {
        // Delete the database
        TestUtils.deleteDatabase(url, dbName);
    }

    @Test
    public void testGetLicenseSummary() throws Exception {
        List<License> summary = handler.getLicenseSummary();

        assertEquals(summary.size(), licenses.size());

        for (License license : summary) {
            // Only those two fields should be present
            assertNotNull(license.id);
            assertNotNull(license.fullname);
            // The rest should not be set
            assertFalse(license.isSetTodoDatabaseIds());
            assertFalse(license.isSetTodos());
            assertFalse(license.isSetReviewdate());
        }
    }

    @Test
    public void testGetLicense() throws Exception {
        License expLicense = licenses.get("Apache 1.1");
        License actLicense = handler.getByID(expLicense.getId(), user.getDepartment());

        assertEquals(expLicense.getId(), actLicense.getId());
        assertEquals(expLicense.getFullname(), actLicense.getFullname());
        assertEquals(expLicense.getTodoDatabaseIdsSize(), actLicense.getTodosSize());

        // Check obligations
        for (String id : expLicense.getTodoDatabaseIds()) {
            Todo actTodo = getById(id, actLicense.getTodos());
            Todo expTodo = getById(id, todos.values());

            // Now check equals
            assertEquals(expTodo.getId(), actTodo.getId());
            assertEquals(expTodo.getText(), actTodo.getText());
            assertEquals(expTodo.getObligationDatabaseIdsSize(), actTodo.getObligationsSize());
            assertEquals(expTodo.getRevision(), actTodo.getRevision());
        }
    }

    @Test(expected = SW360Exception.class)
    public void testGetLicense2() throws Exception {
        // Test non existing ID
        License license = handler.getByID("zzPdtr", user.getDepartment());
        assertNull(license);
    }

    @Test(expected = SW360Exception.class)
    public void testGetLicense3() throws Exception {
        // Test existing ID, but that is not a license
        License license = handler.getByID(todos.values().iterator().next().getId(), user.getDepartment());
        assertNull(license);
    }

    @Test
    public void testGetObligations() throws Exception {
        List<Obligation> actualObligations = handler.getObligations();
        assertEquals(obligations.size(), actualObligations.size());
    }

    public void createTestEntries() throws MalformedURLException {
        // List of test objects
        licenses = new HashMap<>();
        todos = new HashMap<>();
        obligations = new HashMap<>();

        License license1 = new License();
        license1.setId("Apache 1.1");
        license1.setFullname("The Apache Software License, Version 1.1");
        license1.setLicenseType(new LicenseType().setLicenseTypeId(3).setType("Red - copyleft effect"));
        license1.addToRisks(new Risk().setRiskId(123123).setText("If Siemens uses this contractor pattern a long text follows here for reading and display... this might be long.").setCategory(new RiskCategory().setRiskCategoryId(32).setText("Beige")));
        license1.addToRisks(new Risk().setRiskId(1223).setText("Apache 1.1 is noy so risky").setCategory(new RiskCategory().setRiskCategoryId(3123).setText("Green")));
        license1.setReviewdate("10.10.2010");
        license1.addToTodoDatabaseIds("T1");
        license1.addToTodoDatabaseIds("T2");
        license1.addToTodoDatabaseIds("T5");
        licenses.put(license1.id, license1);

        License license2 = new License();
        license2.setId("Apache 2.0");
        license2.setFullname("The Apache Software License, Version 2.0");
        license2.setReviewdate("12.12.2012");
        license2.addToTodoDatabaseIds("T3");
        license2.addToTodoDatabaseIds("T4");
        licenses.put(license2.id, license2);

        Todo todo1 = new Todo().setId("T1").setText("You must include the acknowledgement as part of the documentation for the end user. An example looks as following:  This product includes software developed by the Apache Software Foundation (http://www.apache.org/).");
        todo1.addToObligationDatabaseIds("O1");
        todo1.addToObligationDatabaseIds("O2");
        Todo todo2 = new Todo().setId("T2").setText("You must not names listed in in the license at paragraph 4 (for example Apache and Apache Software Foundation) neither in the documentation nor for ads or marketing.");
        todo2.addToObligationDatabaseIds("O3");
        Todo todo3 = new Todo().setId("T3").setText("Then you must add the following sentence in the header of any modified/added file: 'Code modifications by Siemens AG are under Siemens license conditions'");
        Todo todo4 = new Todo().setId("T4").setText("You must include a prominent notice in the header of all modified files in the following form: © Siemens AG, [year]");
        todo4.addToObligationDatabaseIds("O1");
        todo4.addToObligationDatabaseIds("O4");
        Todo todo5 = new Todo().setId("T5").setText("With the Apache License 2.0,no copyleft effect for proprietary code exists. For proprietary Siemens modifications you can choose the license (meaning applying the Apache 2.0 license or any other license)");
        todo5.addToObligationDatabaseIds("O4");

        todos.put("T1", todo1);
        todos.put("T2", todo2);
        todos.put("T3", todo3);
        todos.put("T4", todo4);
        todos.put("T5", todo5);

        obligations.put("O1", new Obligation().setId("O1").setName("Provide acknowledgements in documentation"));
        obligations.put("O2", new Obligation().setId("O2").setName("Advertising materials are restricted subject to limitations"));
        obligations.put("O3", new Obligation().setId("O3").setName("Documentation that represent additional requirements in case of modifications (for example notice file with author's name)"));
        obligations.put("O4", new Obligation().setId("O4").setName("Apache Copyleft effect"));

        DatabaseConnector db = new DatabaseConnector(url, dbName);

        // Add obligations to database
        for (Obligation obligation : obligations.values()) {
            db.add(obligation);
        }

        // Add todos to database
        for (Todo todo : todos.values()) {
            db.add(todo);
        }

        // Finally, add the licenses to the database
        for (License license : licenses.values()) {
            db.add(license);
        }

    }

}
