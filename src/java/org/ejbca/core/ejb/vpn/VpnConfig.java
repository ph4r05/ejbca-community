package org.ejbca.core.ejb.vpn;

import org.apache.log4j.Logger;
import org.ejbca.config.EjbcaConfigurationHolder;

import java.io.File;
import java.io.IOException;

import static org.ejbca.core.ejb.vpn.VpnCons.VPN_DATA;

/**
 * VPN related configuration.
 *
 * @author ph4r05
 * Created by dusanklinec on 12.01.17.
 */
public class VpnConfig {
    private static final Logger log = Logger.getLogger(VpnConfig.class);

    public static final String CONFIG_VPN_CA = "vpn.ca";
    public static final String CONFIG_VPN_CLIENT_END_PROFILE = "vpn.client.endprofile";
    public static final String CONFIG_VPN_SERVER_END_PROFILE = "vpn.server.endprofile";
    public static final String CONFIG_VPN_KEYSTORE_PASS = "vpn.keystorepass";
    public static final String CONFIG_VPN_KEY_TYPE = "vpn.key.type";
    public static final String CONFIG_VPN_KEY_SIZE = "vpn.key.size";
    public static final String CONFIG_VPN_TEMPLATE_DIR = "vpn.templatedir";
    public static final String CONFIG_VPN_LANGUAGE_DIR = "vpn.languagedir";
    public static final String CONFIG_VPN_DEFAULT_LANG = "vpn.defaultlang";
    public static final String CONFIG_VPN_EMAIL_FROM = "vpn.email.from";
    public static final String CONFIG_VPN_CRL_OVERLAP_MILLI = "vpn.crl.overlapmilli";
    public static final String CONFIG_VPN_CRL_DIR = "vpn.crl.dir";
    public static final String CONFIG_VPN_CRL_MOVE = "vpn.crl.move";

    public static String getDefaultIfEmpty(String src, String defaultValue){
        return (src == null || src.isEmpty()) ? defaultValue : src;
    }

    /**
     * Generic configuration getter.
     * @param key config key
     * @return configuration value or null if not found
     */
    public static String getSetting(String key){
        return getSetting(key, null);
    }

    /**
     * Generic configuration getter
     * @param key config key
     * @param defaultValue default value or null
     * @return config value or default value
     */
    public static String getSetting(String key, String defaultValue){
        return getDefaultIfEmpty(EjbcaConfigurationHolder.getExpandedString(key), defaultValue);
    }

    /**
     * The configured VPN CA name.
     * @return CA name
     */
    public static String getCA() {
        return getDefaultIfEmpty(EjbcaConfigurationHolder.getExpandedString(CONFIG_VPN_CA),
                VpnCons.DEFAULT_CA);
    }

    /**
     * The configured end entity profile name for VPN clients.
     * @return client end entity profile name
     */
    public static String getClientEndEntityProfile() {
        return getDefaultIfEmpty(EjbcaConfigurationHolder.getExpandedString(CONFIG_VPN_CLIENT_END_PROFILE),
                VpnCons.DEFAULT_END_ENTITY_PROFILE);
    }

    /**
     * The configured end entity profile name for VPN server.
     * @return server end entity profile name
     */
    public static String getServerEndEntityProfile() {
        return getDefaultIfEmpty(EjbcaConfigurationHolder.getExpandedString(CONFIG_VPN_SERVER_END_PROFILE),
                VpnCons.DEFAULT_END_ENTITY_PROFILE_SERVER);
    }

    /**
     * The configured VpnUser KeyStore password.
     * @return key store password
     */
    public static String getKeyStorePass(){
        return getDefaultIfEmpty(EjbcaConfigurationHolder.getExpandedString(CONFIG_VPN_KEYSTORE_PASS),
                VpnCons.DEFAULT_KEYSTORE_PASS);
    }

    /**
     * The configured VPN certificate key spec (e.g., RSA).
     * @return key type string
     */
    public static String getKeySpec(){
        return getDefaultIfEmpty(EjbcaConfigurationHolder.getExpandedString(CONFIG_VPN_KEY_TYPE),
                VpnCons.DEFAULT_KEY_ALGORITHM);
    }

    /**
     * The configured VPN certificate key size (2048 by default).
     * @return key size string
     */
    public static String getKeySize(){
        return getDefaultIfEmpty(EjbcaConfigurationHolder.getExpandedString(CONFIG_VPN_KEY_SIZE),
                VpnCons.DEFAULT_KEY_SIZE);
    }

    /**
     * Default language for VPN related stuff, e.g. emails.
     * @return key size string
     */
    public static String getDefaultLanguage(){
        return getDefaultIfEmpty(EjbcaConfigurationHolder.getExpandedString(CONFIG_VPN_DEFAULT_LANG),
                VpnCons.DEFAULT_LANGUAGE);
    }

    /**
     * Return environment variable EJBCA_HOME or an empty string if the variable
     * isn't set.
     *
     * @return Environment variable EJBCA_HOME
     */
    public static String getHomeDir() {
        String ejbcaHomeDir = System.getenv("EJBCA_HOME");
        if (ejbcaHomeDir == null) {
            ejbcaHomeDir = "";
        } else if (!ejbcaHomeDir.endsWith("/") && !ejbcaHomeDir.endsWith("\\")) {
            ejbcaHomeDir += File.separatorChar;
        }
        return ejbcaHomeDir;
    }

    /**
     * Returns sub-directory in the EJBCA home dir.
     * @param directory subdirectory
     * @return EJBCA home sub directory
     * @throws IOException if new File() fails
     */
    protected static File getHomeSubDir(String directory) throws IOException {
        final File dir = new File(getHomeDir(), directory).getCanonicalFile();
        dir.mkdir();
        return dir;
    }

    /**
     * Returns default template directory.
     *
     * @return File representing the directory.
     * @throws IOException if directory op fails
     */
    public static File getDefaultTemplateDir() throws IOException {
        return getHomeSubDir(VpnCons.DEFAULT_TEMPLATE_DIR);
    }

    /**
     * Returns default language property files directory.
     *
     * @return File representing the directory.
     * @throws IOException if directory op fails
     */
    public static File getDefaultLangDir() throws IOException {
        return getHomeSubDir(VpnCons.DEFAULT_LANGUAGE_DIR);
    }

    /**
     * Returns directory with templates for sending emails.
     * @return template directory
     */
    public static File getTemplateDir() throws IOException {
        final String tplDirConf = EjbcaConfigurationHolder.getExpandedString(CONFIG_VPN_TEMPLATE_DIR);
        if (tplDirConf != null && !tplDirConf.isEmpty()){
            final File tplDir = new File(tplDirConf).getCanonicalFile();
            tplDir.mkdirs();
            return tplDir;
        }

        return getDefaultTemplateDir();
    }

    /**
     * Returns directory with language property files.
     * @return template directory
     */
    public static File getLanguageDir() throws IOException {
        final String tplDirConf = EjbcaConfigurationHolder.getExpandedString(CONFIG_VPN_LANGUAGE_DIR);
        if (tplDirConf != null && !tplDirConf.isEmpty()){
            final File tplDir = new File(tplDirConf).getCanonicalFile();
            tplDir.mkdirs();
            return tplDir;
        }

        return getDefaultLangDir();
    }

    /**
     * Returns public HTTPS web port.
     * @return integer port number
     */
    public static int getPublicHttpsPort(){
        final int DEFAULT_PORT = 8442;
        final String portStr = EjbcaConfigurationHolder.getString("httpserver.pubhttps");

        int port = DEFAULT_PORT;
        if (portStr != null && !portStr.isEmpty()) {
            try{
                port = Integer.parseInt(portStr);
            } catch(Exception e){
                log.error("Exception in parsing port number", e);
            }
        }

        return port;
    }

    /**
     * Returns server hostname from the settings.
     * @return
     */
    public static String getServerHostname(){
        final String settingHostName = EjbcaConfigurationHolder.getString("httpsserver.hostname");
        return settingHostName;
    }

    /**
     * Returns fromAddress to put on emails sent by the VPN module.
     * By default tries to load CONFIG_VPN_EMAIL_FROM, if that is empty / null,
     * tries general one mail.from.
     * @return email address to put in the from field or null
     */
    public static String getEmailFromAddress(){
        final String vpnEmailFrom = getSetting(CONFIG_VPN_EMAIL_FROM);
        if (vpnEmailFrom != null && !vpnEmailFrom.isEmpty()){
            return vpnEmailFrom;
        }

        final String genericFrom = EjbcaConfigurationHolder.getString("mail.from");
        if (genericFrom != null && !genericFrom.isEmpty()){
            return genericFrom;
        }

        return null;
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
    public static File getVpnDataDir(String directory) throws IOException {
        File dir = null;
        if (directory == null || directory.isEmpty()) {
            dir = new File(getHomeDir(), VPN_DATA);
        } else {
            dir = new File(directory).getCanonicalFile();
        }

        dir.mkdirs();
        return dir;
    }

    /**
     * Returns configured overlap for the VPN CRL.
     * @return time in milliseconds the CRL should be generated before the previous one expires.
     */
    public static long getDefaultCRLOverlapMilli(){
        final String milliPrefs = EjbcaConfigurationHolder.getExpandedString(CONFIG_VPN_CRL_OVERLAP_MILLI);
        if (milliPrefs == null){
            return VpnCons.DEFAULT_VPN_OVERLAP;
        }

        try{
            return Long.parseLong(milliPrefs);
        } catch(Exception e){
            log.error("Exception in parsing VPN CRL verlap milli number", e);
        }

        return VpnCons.DEFAULT_VPN_OVERLAP;
    }

    /**
     * Returns configured directory for the CRL.
     * If the CRL directory is not defined, default VPN directory is used.
     * @return CRL data directory
     */
    public static File getCrlDirectory() throws IOException {
        final String crlDirPrefs = EjbcaConfigurationHolder.getExpandedString(CONFIG_VPN_DEFAULT_LANG);
        if (crlDirPrefs == null){
            return getVpnDataDir(null);
        }

        final File dir = new File(crlDirPrefs);
        dir.mkdirs();
        return dir;
    }


    /**
     * Returns if CRL generation should use generate-to-temporary-file-then-move strategy or open
     * directly the configured CRL file and pverwrite that.
     *
     * @return true if move strategy should be used.
     */
    public static boolean shouldUseMoveForCrlGeneration(){
        final String movePref = EjbcaConfigurationHolder.getExpandedString(CONFIG_VPN_CRL_MOVE);
        if (movePref == null){
            return VpnCons.DEFAULT_VPN_CRL_MOVE;
        }

        try{
            return Boolean.parseBoolean(movePref);
        } catch(Exception e){
            log.error("Exception in parsing VPN move CRL", e);
        }

        return VpnCons.DEFAULT_VPN_CRL_MOVE;
    }
}
