<%--
  ~ Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
  ~
  ~ This program is free software; you can redistribute it and/or modify it under
  ~ the terms of the GNU General Public License Version 2.0 as published by the
  ~ Free Software Foundation with classpath exception.
  ~
  ~ This program is distributed in the hope that it will be useful, but WITHOUT
  ~ ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  ~ FOR A PARTICULAR PURPOSE. See the GNU General Public License version 2.0 for
  ~ more details.
  ~
  ~ You should have received a copy of the GNU General Public License along with
  ~ this program (please see the COPYING file); if not, write to the Free
  ~ Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
  ~ 02110-1301, USA.
  --%>
<%@include file="/html/init.jsp" %>


<%@ taglib prefix="sw360" uri="/WEB-INF/customTags.tld" %>

<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<jsp:useBean id="attachments" type="java.util.Set<com.siemens.sw360.datahandler.thrift.attachments.Attachment>" scope="request"/>
<jsp:useBean id="documentType" type="java.lang.String" scope="request"/>
<table class="table info_table " id="attachmentDetail" title="Attachment Information">
    <thead>
    <tr>
        <th colspan="6" class="headlabel">Attachments</th>
    </tr>
    </thead>
    <tbody>
    <core_rt:if test="${not empty attachments}">
        <core_rt:forEach items="${attachments}" var="attachment" varStatus="loop">
            <tr id="attachmentRow1${loop.count}">
                <td colspan="2" class="attachmentTitle">
                    <sw360:DisplayDownloadAttachment id="${attachment.attachmentContentId}" name="${attachment.filename}"/>
                    "<sw360:out value="${attachment.filename}"/>"
                </td>
                <td rowspan="2">Uploader</td>
                <td colspan="3" rowspan="2">
                    Comment: "<sw360:out value="${attachment.createdComment}"/>" <br/>
                    <sw360:out value="${attachment.createdBy}"/>,
                    <sw360:out value= "${attachment.createdTeam}"/>,
                    <sw360:out value="${attachment.createdOn}"/>
                </td>
            </tr>
            <tr id="attachmentRow2${loop.count}" >
                <td colspan="2" rowspan="3" class="lessPadding">
                    Type: <sw360:DisplayEnum value="${attachment.attachmentType}"/> <br/>
                    Status: <sw360:DisplayEnum value="${attachment.checkStatus}"/> <br/>
                    SHA1-Checksum: <input class="checksumInput" readonly="true"
                           type="text"
                           value="${attachment.sha1}">
                </td>
            </tr>
            <tr id="attachmentRow3${loop.count}" >
                <td rowspan="2">Approver</td>
                <td colspan="3" rowspan="2">
                    <core_rt:if test="${not empty attachment.checkedBy}">
                        Comment: "<sw360:out value="${attachment.checkedComment}"/>" <br/>
                        <sw360:out value="${attachment.checkedBy}"/>,
                        <sw360:out value="${attachment.checkedTeam}"/>,
                        <sw360:out value="${attachment.checkedOn}"/>
                    </core_rt:if>
                    <core_rt:if test="${empty attachment.checkedBy}">
                        -<br/>
                         <br/>
                    </core_rt:if>
                </td>
            </tr>
            <tr id="attachmentRow4${loop.count}" class="lastAttachmentRow">
            </tr>
        </core_rt:forEach>
    </core_rt:if>

    <core_rt:if test="${empty attachments}">
        <tr id="noAttachmentsRow">
            <td colspan="6">No attachments yet.</td>
        </tr>
    </core_rt:if>
    </tbody>
</table>
