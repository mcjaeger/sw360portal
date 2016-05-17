/*
 * Copyright Siemens AG, 2014-2016. Part of the SW360 Portal Project.
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
package com.siemens.sw360.datahandler.thrift;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.siemens.sw360.datahandler.couchdb.AttachmentContentWrapper;
import com.siemens.sw360.datahandler.couchdb.DocumentWrapper;
import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentContent;
import com.siemens.sw360.datahandler.thrift.components.ClearingInformation;
import com.siemens.sw360.datahandler.thrift.components.Component;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.components.Repository;
import com.siemens.sw360.datahandler.thrift.fossology.FossologyHostFingerPrint;
import com.siemens.sw360.datahandler.thrift.licenses.*;
import com.siemens.sw360.datahandler.thrift.moderation.ModerationRequest;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.vendors.Vendor;
import com.siemens.sw360.datahandler.thrift.vulnerabilities.CVEReference;
import com.siemens.sw360.datahandler.thrift.vulnerabilities.ReleaseVulnerabilityRelation;
import com.siemens.sw360.datahandler.thrift.vulnerabilities.VendorAdvisory;
import com.siemens.sw360.datahandler.thrift.vulnerabilities.Vulnerability;
import org.apache.log4j.Logger;
import org.apache.thrift.TBase;
import org.apache.thrift.TFieldIdEnum;
import org.ektorp.util.Documents;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Utility class to supplement the Thrift generated code
 *
 * @author cedric.bodet@tngtech.com
 */
public class ThriftUtils {
    private static final Logger log = Logger.getLogger(ThriftUtils.class);

    public static final List<Class> THRIFT_CLASSES = ImmutableList.<Class>builder()
            .add(Attachment.class) // Attachment service
            .add(AttachmentContent.class) // Attachment service
            .add(Component.class).add(Release.class) // Component service
            .add(License.class).add(Todo.class).add(Obligation.class) // License service
            .add(LicenseType.class).add(Risk.class).add(RiskCategory.class) // License service
            .add(Project.class) // Project service
            .add(User.class) // User service
            .add(Vendor.class) // Vendor service
            .add(ModerationRequest.class) // Moderation service‚
            .add(FossologyHostFingerPrint.class) // Fossology service
            .add(Vulnerability.class, ReleaseVulnerabilityRelation.class) // Vulnerability Service
            .build();

    public static final List<Class> THRIFT_NESTED_CLASSES = ImmutableList.<Class>builder()
            .add(Repository.class).add(ClearingInformation.class) // Component service
            .add(CVEReference.class, VendorAdvisory.class) // Vulnerability Service
            .build();

    private static final Map<Class, Class<? extends DocumentWrapper>> THRIFT_WRAPPED =
            ImmutableMap.<Class, Class<? extends DocumentWrapper>>builder()
                    .put(AttachmentContent.class, AttachmentContentWrapper.class)
                    .build();

    private ThriftUtils() {
        // Utility class with only static functions
    }

    public static boolean isMapped(Class clazz) {
        return THRIFT_WRAPPED.containsKey(clazz);
    }

    public static Class<? extends DocumentWrapper> getWrapperClass(Class clazz) {
        return THRIFT_WRAPPED.get(clazz);
    }

    public static <T extends TBase<T, F>, F extends TFieldIdEnum> void copyField(T src, T dest, F field) {
        if (src.isSet(field)) {
            dest.setFieldValue(field, src.getFieldValue(field));
        } else {
            dest.setFieldValue(field, null);
        }
    }


    public static <T extends TBase<T, F>, F extends TFieldIdEnum> void copyFields(T src, T dest, Iterable<F> fields) {
        for (F field : fields) {
            copyField(src, dest, field);
        }
    }

    public static <S extends TBase<S, FS>, FS extends TFieldIdEnum, D extends TBase<D, FD>, FD extends TFieldIdEnum> void copyField2(S src, D dest, FS srcField, FD destField) {
        if (src.isSet(srcField)) {
            dest.setFieldValue(destField, src.getFieldValue(srcField));
        } else {
            dest.setFieldValue(destField, null);
        }
    }

    public static Function<Object, String> extractId() {
        return new Function<Object, String>() {
            @Override
            public String apply(Object input) {
                return Documents.getId(input);
            }
        };
    }

    public static <T> Map<String, T> getIdMap(Collection<T> in) {
        return Maps.uniqueIndex(in, extractId());
    }

    public static <T extends TBase<T, F>, F extends TFieldIdEnum> Function<T, Object> extractField(final F field) {
        return extractField(field, Object.class);
    }

    public static <T extends TBase<T, F>, F extends TFieldIdEnum, R> Function<T, R> extractField(final F field, final Class<R> clazz) {
        return new Function<T, R>() {
            @Override
            public R apply(T input) {
                if (input.isSet(field)) {
                    Object fieldValue = input.getFieldValue(field);
                    if (clazz.isInstance(fieldValue)) {
                        @SuppressWarnings("unchecked")
                        R value = (R) fieldValue;
                        return value;
                    } else {
                        log.error("field " + field + " of " + input + " cannot be cast to" + clazz.getSimpleName());
                        return null;
                    }
                } else {
                    return null;
                }
            }
        };
    }

    public static Iterable<Component._Fields> immutableOfComponent() {
        return ImmutableList.of(
                Component._Fields.CREATED_BY,
                Component._Fields.CREATED_ON
        );
    }

    public static Iterable<Release._Fields> immutableOfRelease() {
        return ImmutableList.of(
                Release._Fields.CREATED_BY,
                Release._Fields.CREATED_ON,
                Release._Fields.FOSSOLOGY_ID,
                Release._Fields.ATTACHMENT_IN_FOSSOLOGY,
                Release._Fields.CLEARING_TEAM_TO_FOSSOLOGY_STATUS
        );
    }

    public static Iterable<Release._Fields> immutableOfReleaseForFossology() {
        return ImmutableList.of(
                Release._Fields.CREATED_BY,
                Release._Fields.CREATED_ON
        );
    }
}
