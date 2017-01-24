/*************************************************************************
 *                                                                       *
 *  EJBCA Community: The OpenSource Certificate Authority                *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
 
package org.ejbca.ui.cli.vpn;

import org.apache.log4j.Logger;
import org.cesecore.authorization.AuthorizationDeniedException;
import org.cesecore.certificates.ca.CADoesntExistsException;
import org.cesecore.certificates.ca.CAInfo;
import org.cesecore.certificates.ca.CaSessionRemote;
import org.cesecore.certificates.certificate.CertificateConstants;
import org.cesecore.certificates.certificateprofile.CertificateProfileSessionRemote;
import org.cesecore.util.EjbRemoteHelper;
import org.cesecore.util.StringTools;
import org.ejbca.core.ejb.ra.raadmin.EndEntityProfileSessionRemote;
import org.ejbca.core.ejb.vpn.VpnConfig;
import org.ejbca.core.ejb.vpn.VpnUtils;
import org.ejbca.core.model.ra.raadmin.EndEntityProfileNotFoundException;
import org.ejbca.ui.cli.infrastructure.command.CommandResult;
import org.ejbca.ui.cli.infrastructure.command.EjbcaCliUserCommandBase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Base for VPN commands, contains common functions for VPN operation.
 *
 * @author ph4r05
 */
public abstract class BaseVpnCommand extends EjbcaCliUserCommandBase {
    private static final Logger log = Logger.getLogger(BaseVpnCommand.class);

	public static final String MAINCOMMAND = "vpn";

    /**
     * VPN data dir - default one.
     */
    public static final String VPN_DATA = "vpn";
	
    @Override
    public String[] getCommandPath() {
        return new String[] { MAINCOMMAND };
    }

    /**
     * Returns a cached remote session bean.
     *
     * @param key the @Remote-appended interface for this session bean
     * @return the sought interface, or null if it doesn't exist in JNDI context.
     */
    public static <T> T getRemoteSession(final Class<T> key) {
        return EjbRemoteHelper.INSTANCE.getRemoteSession(key);
    }

    /**
     * VPN end entity server profile.
     * @return
     * @throws EndEntityProfileNotFoundException
     */
    protected int getVpnServerEndEntityProfile() throws EndEntityProfileNotFoundException {
        return EjbRemoteHelper.INSTANCE.getRemoteSession(EndEntityProfileSessionRemote.class)
                .getEndEntityProfileId(VpnConfig.getServerEndEntityProfile());
    }

    /**
     * VPN end entity client profile.
     * @return
     * @throws EndEntityProfileNotFoundException
     */
    protected int getVpnClientEndEntityProfile() throws EndEntityProfileNotFoundException {
        return EjbRemoteHelper.INSTANCE.getRemoteSession(EndEntityProfileSessionRemote.class)
                .getEndEntityProfileId(VpnConfig.getClientEndEntityProfile());
    }

    /**
     * Returns VPN CA.
     * @return
     * @throws AuthorizationDeniedException
     * @throws CADoesntExistsException
     */
    protected CAInfo getVpnCA() throws AuthorizationDeniedException, CADoesntExistsException {
        return EjbRemoteHelper.INSTANCE.getRemoteSession(CaSessionRemote.class)
                .getCAInfo(getAuthenticationToken(), VpnConfig.getCA());
    }

    /**
     * Returns list of all CAs.
     * @return
     * @throws AuthorizationDeniedException
     */
    protected List<CAInfo> getCAs() throws AuthorizationDeniedException {
        final Collection<Integer> cas = EjbRemoteHelper.INSTANCE.getRemoteSession(CaSessionRemote.class)
                .getAuthorizedCaIds(getAuthenticationToken());
        final List<CAInfo> infoList = new ArrayList<>(cas.size());

        try {
            for (int caid : cas) {
                CAInfo info = EjbRemoteHelper.INSTANCE.getRemoteSession(CaSessionRemote.class)
                        .getCAInfo(getAuthenticationToken(), caid);
                infoList.add(info);
            }
        } catch (CADoesntExistsException e) {
            throw new IllegalStateException("CA couldn't be retrieved even though it was just referenced.");
        }

        return infoList;
    }

    /**
     * Adds list of available CAs to the string builder (for help)
     * @param sb
     */
    protected void addAvailableCas(StringBuilder sb){
        String existingCas = "";

        // Get existing CAs
        Collection<Integer> cas = EjbRemoteHelper.INSTANCE.getRemoteSession(CaSessionRemote.class).getAuthorizedCaIds(getAuthenticationToken());
        try {
            for (int caid : cas) {
                CAInfo info = EjbRemoteHelper.INSTANCE.getRemoteSession(CaSessionRemote.class).getCAInfo(getAuthenticationToken(), caid);
                existingCas += (existingCas.length() == 0 ? "" : ", ") + "\"" + info.getName() + "\"";
            }
        } catch (AuthorizationDeniedException e) {
            existingCas = "ERROR: CLI user not authorized to fetch available CAs>";
        } catch (CADoesntExistsException e) {
            throw new IllegalStateException("CA couldn't be retrieved even though it was just referenced.");
        }
        sb.append("Existing CAs: " + existingCas + "\n");
    }

    /**
     * Adds list of available end entity profiles to the string builder (for help)
     * @param sb
     */
    protected void addAvailableEndProfiles(StringBuilder sb){
        String endEntityProfiles = "";
        Collection<Integer> eps = EjbRemoteHelper.INSTANCE.getRemoteSession(EndEntityProfileSessionRemote.class).getAuthorizedEndEntityProfileIds(
                getAuthenticationToken());
        for (int epid : eps) {
            endEntityProfiles += (endEntityProfiles.length() == 0 ? "" : ", ") + "\""
                    + EjbRemoteHelper.INSTANCE.getRemoteSession(EndEntityProfileSessionRemote.class).getEndEntityProfileName(epid) + "\"";
        }
        sb.append("End entity profiles: " + endEntityProfiles + "\n");
    }

    /**
     * Adds list of available certificate profiles to the string builder (for help)
     * @param sb
     */
    protected void addAvailableCertProfiles(StringBuilder sb){
        String certificateProfiles = "";
        Collection<Integer> cps = EjbRemoteHelper.INSTANCE.getRemoteSession(CertificateProfileSessionRemote.class)
                .getAuthorizedCertificateProfileIds(getAuthenticationToken(), CertificateConstants.CERTTYPE_ENDENTITY);
        for (int cpid : cps) {
            certificateProfiles += (certificateProfiles.length() == 0 ? "" : ", ") + "\""
                    + EjbRemoteHelper.INSTANCE.getRemoteSession(CertificateProfileSessionRemote.class).getCertificateProfileName(cpid) + "\"";
        }
        sb.append("Certificate profiles: " + certificateProfiles + "\n");
    }

    /**
     * Adds available CAs, profiles to the string builder - for extended help.
     * @param sb string builder to add stuff to
     */
    protected void addAvailableStuff(StringBuilder sb){
        // Get existing CAs
        addAvailableCas(sb);

        // Get End entity profiles
        addAvailableEndProfiles(sb);

        // Get Cert profiles
        addAvailableCertProfiles(sb);
    }

    /**
     * Return environment variable EJBCA_HOME or an empty string if the variable
     * isn't set.
     *
     * @return Environment variable EJBCA_HOME
     */
    protected static String getHomeDir() {
        String ejbcaHomeDir = System.getenv("EJBCA_HOME");
        if (ejbcaHomeDir == null) {
            ejbcaHomeDir = "";
        } else if (!ejbcaHomeDir.endsWith("/") && !ejbcaHomeDir.endsWith("\\")) {
            ejbcaHomeDir += File.separatorChar;
        }
        return ejbcaHomeDir;
    }

    /**
     * Prompts for the password if not set on command line
     * @param commandLineArgument
     * @return
     */
    protected String getAuthenticationCode(final String commandLineArgument) {
        final String authenticationCode;
        if (commandLineArgument == null || "null".equalsIgnoreCase(commandLineArgument)) {
            getLogger().info("Enter password: ");
            getLogger().info("");
            authenticationCode = StringTools.passwordDecryption(String.valueOf(System.console().readPassword()), "End Entity Password");
        } else {
            authenticationCode = StringTools.passwordDecryption(commandLineArgument, "End Entity Password");
        }
        return authenticationCode;
    }

    /**
     * Returns VPN data directory to store VPN related files (e.g., certificates, p12, ...).
     * If directory does not exist, it will be created.
     * If null/empty string is given, EJBCA_HOME/vpn is used.
     *
     * @param directory directory
     * @return File representing the directory.
     * @throws IOException
     */
    protected File getVpnDataDir(String directory) throws IOException {
        if (directory == null || directory.isEmpty()) {
            directory = getHomeDir() + VPN_DATA;
        }

        final File dir = new File(directory).getCanonicalFile();
        dir.mkdirs();
        return dir;
    }

    /**
     * Returns true if entered parameters are non-empty and have valid format.
     * @param email
     * @param device
     * @return
     */
    protected boolean isEmailAndDeviceValid(String email, String device){
        if (email == null || email.isEmpty()){
            log.error("Email cannot be empty");
            return false;
        }
        if (device == null || device.isEmpty()){
            log.error("Device cannot be empty");
            return false;
        }
        if (!VpnUtils.isEmailValid(email)){
            log.error("Email is invalid");
            return false;
        }

        return true;
    }
}
