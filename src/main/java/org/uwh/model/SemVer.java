package org.uwh.model;

import java.util.Objects;


public class SemVer implements Comparable<SemVer> {
  private int major;
  private int minor;
  private int patch;

  public SemVer(int major, int minor, int patch) {
    this.major = major;
    this.minor = minor;
    this.patch = patch;
  }

  public static SemVer of(int major, int minor) {
    return new SemVer(major, minor, 0);
  }

  public static SemVer of(int major, int minor, int patch) {
    return new SemVer(major, minor, patch);
  }

  public static SemVer of(String version) {
    String[] parts = version.split("\\.");
    int major = Integer.parseInt(parts[0]);
    int minor = (parts.length > 1) ? Integer.parseInt(parts[1]) : 0;
    int patch = (parts.length > 2) ? Integer.parseInt(parts[2]) : 0;
    return new SemVer(major, minor, patch);
  }

  @Override
  public int compareTo(SemVer other) {
    if (major < other.major) {
      return -1;
    } else if (major > other.major) {
      return 1;
    } else if (minor < other.minor) {
      return -1;
    } else if (minor > other.minor) {
      return 1;
    } else {
      return Integer.compare(patch, other.patch);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SemVer semVer = (SemVer) o;
    return major == semVer.major && minor == semVer.minor && patch == semVer.patch;
  }

  @Override
  public int hashCode() {
    return Objects.hash(major, minor, patch);
  }
}
