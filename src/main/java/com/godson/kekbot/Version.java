package com.godson.kekbot;

import com.godson.kekbot.util.Utils;

public class Version {
    private int majorVersion;
    private int minorVersion;
    private int patchVersion;
    private int betaVersion;

    public Version(int majorVersion, int minorVersion, int patchVersion, int betaVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.patchVersion = patchVersion;
        this.betaVersion = betaVersion;
    }

    public Version(int majorVersion, int minorVersion, int patchVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.patchVersion = patchVersion;
        this.betaVersion = 0;
    }

    private Version(int majorVersion, int minorVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.patchVersion = 0;
        this.betaVersion = 0;
    }

    Version(int majorVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = 0;
        this.patchVersion = 0;
        this.betaVersion = 0;
    }

    public static Version fromString(String version) {
        String[] parts = version.split("\\.");
        if (parts.length == 4) {
            return new Version(Utils.parseInt(parts[0], 1), Utils.parseInt(parts[1], 0), Utils.parseInt(parts[2], 0), Utils.parseInt(parts[3], 1));
        } else if (parts.length == 3) {
            return new Version(Utils.parseInt(parts[0], 1), Utils.parseInt(parts[1], 0), Utils.parseInt(parts[2], 0));
        } else if (parts.length == 2) {
            return new Version(Utils.parseInt(parts[0], 1), Utils.parseInt(parts[1], 0));
        } else if (parts.length == 1) {
            return new Version(Utils.parseInt(parts[0], 1));
        }
        return new Version(1);
    }

    /**
     * Compares the version to another one
     *
     * @param version the version to compare it with
     * @return true if this is higher than version
     */
    public boolean isHigherThan(Version version) {
        if (version == null || this.getMajorVersion() > version.getMajorVersion()) {
            return true;
        } else if (this.getMajorVersion() == version.getMajorVersion()) {
            if (this.getMinorVersion() > version.getMinorVersion()) {
                return true;
            } else if (this.getMinorVersion() == version.getMinorVersion()) {
                if (this.getPatchVersion() > version.getPatchVersion()) {
                    return true;
                } else if (this.getPatchVersion() == version.getPatchVersion()) {
                    if (this.getBetaVersion() > version.getBetaVersion())
                        return true;
                }
            }
        }
        return false;
    }

    public int getPatchVersion() {
        return patchVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getBetaVersion() {
        return betaVersion;
    }

    @Override
    public String toString() {
        return getMajorVersion() + "." + getMinorVersion() + "." + getPatchVersion() + (betaVersion > 0 ? "-BETA" + betaVersion : "");
    }
}
