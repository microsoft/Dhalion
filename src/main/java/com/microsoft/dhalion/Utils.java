package com.microsoft.dhalion;

import java.util.ArrayList;

import static java.awt.SystemColor.text;

public class Utils {

  public static String getCompositeName(String... names) {
    if (names.length > 0) {
      String text = names[0];
      for (int i = 1; i < names.length; i++) {
        text = text + "_" + names[i];
      }
      return text;
    }
    return "";
  }

}
