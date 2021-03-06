
/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
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
package com.siemens.sw360.portal.tags;

import com.google.common.base.Strings;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.thrift.ThriftClients;
import com.siemens.sw360.datahandler.thrift.components.ReleaseClearingStateSummary;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.users.UserService;
import com.siemens.sw360.portal.common.JsonHelpers;
import com.siemens.sw360.portal.common.ThriftJsonSerializer;
import com.siemens.sw360.portal.users.UserUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.taglibs.standard.tag.common.core.OutSupport;
import org.apache.thrift.TException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.regex.Matcher.quoteReplacement;

/**
 * This prepares the description for display
 *
 * @author birgit.heydenreich@tngtech.com
 */

public class DisplayDescription extends SimpleTagSupport {

    private String description;
    private String jsQuoting ="";
    private int maxChar=140;

    public void setDescription(String description) {
        this.description = description;
    }
    public void setMaxChar(int maxChar) {
        this.maxChar = maxChar;
    }
    public void setJsQuoting(String jsQuoting) {
        this.jsQuoting = jsQuoting;
    }

    public void doTag() throws JspException, IOException {
        if (maxChar > 4) {
            description = StringUtils.abbreviate(description, maxChar);
        }
        if ("'".equals(jsQuoting)) {
            this.description = escapeInSingleQuote(description);
        } else if ("\"".equals(jsQuoting)) {
            this.description = escapeInDoubleQuote(description);
        }
        getJspContext().getOut().print(description);

    }
    private String escapeInDoubleQuote(String value) {
        return value.replaceAll("\"", quoteReplacement("\\\""));
    }

    protected String escapeInSingleQuote(String value) {
        return value.replaceAll("'", quoteReplacement("\\\'"));
    }
}

