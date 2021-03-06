/*
 * Copyright Siemens AG, 2014-2015. Part of the SW360 Portal Project.
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
package com.siemens.sw360.datahandler.permissions;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.thrift.Visibility;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.users.RequestedAction;
import com.siemens.sw360.datahandler.thrift.users.User;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.siemens.sw360.datahandler.common.CommonUtils.toSingletonSet;
import static com.siemens.sw360.datahandler.common.SW360Utils.getBUFromOrganisation;
import static com.siemens.sw360.datahandler.permissions.PermissionUtils.isUserAtLeast;
import static com.siemens.sw360.datahandler.thrift.users.UserGroup.CLEARING_ADMIN;

/**
 * Created by bodet on 16/02/15.
 *
 * @author cedric.bodet@tngtech.com
 */
public class ProjectPermissions extends DocumentPermissions<Project> {

    private final Set<String> moderators;
    private final Set<String> contributors;

    protected ProjectPermissions(Project document, User user) {
        super(document, user);

        moderators = new ImmutableSet.Builder<String>()
                .addAll(toSingletonSet(document.getCreatedBy()))
                .addAll(toSingletonSet(document.getProjectResponsible()))
                .addAll(CommonUtils.nullToEmptySet(document.getModerators()))
                .addAll(CommonUtils.nullToEmptySet(document.getComoderators()))
                .build();
        contributors = new ImmutableSet.Builder<String>()
                .addAll(moderators)
                .addAll(CommonUtils.nullToEmptySet(document.getContributors()))
                .addAll(toSingletonSet(document.getLeadArchitect()))
                .build();
    }

    public static boolean isUserInBU(Project document, String bu) {
        final String buFromOrganisation = getBUFromOrganisation(bu);
        return !isNullOrEmpty(bu) && !isNullOrEmpty(buFromOrganisation)
              && !isNullOrEmpty(document.getBusinessUnit()) && document.getBusinessUnit().startsWith(buFromOrganisation);
    }

    public static boolean userIsEquivalentToModeratorinProject(Project input, String user) {
        final HashSet<String> allowedUsers = new HashSet<>();
        if (input.isSetCreatedBy()) allowedUsers.add(input.getCreatedBy());
        if (input.isSetLeadArchitect()) allowedUsers.add(input.getLeadArchitect());
        if (input.isSetProjectResponsible()) allowedUsers.add(input.getProjectResponsible());
        if (input.isSetModerators()) allowedUsers.addAll(input.getModerators());
        if (input.isSetComoderators()) allowedUsers.addAll(input.getComoderators());
        if (input.isSetContributors()) allowedUsers.addAll(input.getContributors());

        return allowedUsers.contains(user);
    }

    @NotNull
    public static Predicate<Project> isVisible(final User user) {
        return new Predicate<Project>() {
            @Override
            public boolean apply(Project input) {
                Visibility visbility = input.getVisbility();
                if (visbility == null) {
                    visbility = Visibility.BUISNESSUNIT_AND_MODERATORS; // the current default
                }

                switch (visbility) {
                    case PRIVATE:
                        return user.getEmail().equals(input.getCreatedBy());
                    case ME_AND_MODERATORS: {
                        return userIsEquivalentToModeratorinProject(input, user.getEmail());
                    }
                    case BUISNESSUNIT_AND_MODERATORS: {
                        return isUserInBU(input, user.getDepartment()) || userIsEquivalentToModeratorinProject(input, user.getEmail()) || isUserAtLeast(CLEARING_ADMIN, user);
                    }
                    case EVERYONE:
                        return true;
                }

                return false;
            }
        };
    }

    @Override
    public void fillPermissions(Project other, Map<RequestedAction, Boolean> permissions) {
        other.permissions=permissions;
    }

    @Override
    public boolean isActionAllowed(RequestedAction action) {
        if(action==RequestedAction.READ) {
            return isVisible(user).apply(document);
        } else {
            return getStandardPermissions(action);
        }
    }

    @Override
    protected Set<String> getContributors() {
        return contributors;
    }

    @Override
    protected Set<String> getModerators() {
        return moderators;
    }

}
