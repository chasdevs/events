package com.github.chasdevs.events.util;

import com.google.common.base.CaseFormat;

/**
 * This utility was created for a central place to convert between our
 * convention for event file names, registry subject names, and event
 * namespace names.
 * <p>
 * The method names have been generalized to not include the convention, so
 * that we can easily change them here without needing to rename methods.
 */
public class NamingUtil {

    public static final CaseFormat SUBJECT_NAME_FORMAT = CaseFormat.LOWER_HYPHEN;
    public static final CaseFormat FILE_NAME_FORMAT = CaseFormat.UPPER_CAMEL;

    /**
     * Takes a string matching our registry subject name convention and returns a
     * string that matches our file name convention.
     *
     * @param name
     * @return
     */
    public static String fromSubjectToFile(String name) {
        //todo - add regex to confirm input
        return SUBJECT_NAME_FORMAT.to(FILE_NAME_FORMAT, name);
    }

    /**
     * Takes a string matching our file name convention and returns a
     * string that matches our registry subject name convention.
     *
     * @param name
     * @return
     */
    public static String fromFileToSubject(String name) {
        //todo - add regex to confirm input
        return FILE_NAME_FORMAT.to(SUBJECT_NAME_FORMAT, name);
    }

    /**
     * Takes a string matching our registry subject name convention and returns a
     * string that matches our namespace name convention.
     *
     * @param name
     * @return
     */
    public static String fromSubjectToNamespace(String name) {
        //todo - add regex to confirm input
        return name.toLowerCase().replace("-","");
    }

    /**
     * Takes a string matching our local name convention (basically registry subject name convention)
     * and appends the required suffix for value schemas in all Confluent eco-system tools. It simply
     * appends {@link Constants#DEFAULT_SUBJECT_SUFFIX} to the end of the given name.
     *
     * @param name
     * @return
     */
    public static String fromLocalToRegistrySubject(String name) {
        return SUBJECT_NAME_FORMAT.to(SUBJECT_NAME_FORMAT, name.concat(Constants.DEFAULT_SUBJECT_SUFFIX));
    }


}
