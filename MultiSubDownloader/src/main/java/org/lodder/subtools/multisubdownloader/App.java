package org.lodder.subtools.multisubdownloader;

import java.awt.EventQueue;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.UIManager;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.lodder.subtools.multisubdownloader.settings.SettingsControl;
import org.lodder.subtools.sublibrary.logging.Level;
import org.lodder.subtools.sublibrary.logging.Logger;

public class App {

  private final static SettingsControl prefctrl = new SettingsControl();

  /**
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    // Default log level for the program
    Logger.instance.setLogLevel(Level.INFO);

    CommandLineParser parser = new GnuParser();
    HelpFormatter formatter = new HelpFormatter();

    org.apache.commons.cli.CommandLine line = null;
    try {
      line = parser.parse(getCLIOptions(), args);
    } catch (ParseException e) {
      Logger.instance.error(Logger.stack2String(e));
    }

    if (line != null) {
      if (line.hasOption("help")) {
        formatter.printHelp("MultiSubDownloader", getCLIOptions());
      } else {
        if (line.hasOption("debug")) {
          Logger.instance.setLogLevel(Level.DEBUG);
        }
        if (line.hasOption("trace")) {
          Logger.instance.setLogLevel(Level.ALL);
        }
        if (line.hasOption("speedy")) {
          Preferences preferences = Preferences.userRoot();
          preferences.putBoolean("speedy", true);
        } else {
          Preferences preferences = Preferences.userRoot();
          preferences.putBoolean("speedy", false);
        }

        if (line.hasOption("importpreferences")) {
          File file = new File(line.getOptionValue("importpreferences"));
          try {
            if (file.isFile()) {
              prefctrl.importPreferences(file);
            }
          } catch (Exception e) {
            Logger.instance.error("executeArgs: importPreferences" + Logger.stack2String(e));
          }
        } else if (line.hasOption("updatefromonlinemapping")) {
          try {
            prefctrl.updateMappingFromOnline();
            prefctrl.store();
          } catch (Throwable e) {
            Logger.instance.error("executeArgs: updateFromOnlineMapping" + Logger.stack2String(e));
          }
        } else if (line.hasOption("nogui")) {
          CommandLine cmd = new CommandLine(prefctrl);
          if (line.hasOption("folder")) cmd.setFolder(new File(line.getOptionValue("folder")));

          if (line.hasOption("language")) {
            if (line.getOptionValue("language").equals("nl")
                || line.getOptionValue("language").equals("en")) {
              cmd.setLanguagecode(line.getOptionValue("language"));
            } else {
              throw new Exception("Language code not valid must be 'en' or 'nl' ");
            }
          } else {
            Logger.instance.log("No language given using default: 'nl' ");
            cmd.setLanguagecode("nl");
          }
          cmd.setForce(line.hasOption("force"));
          cmd.setDownloadall(line.hasOption("downloadall"));
          cmd.setRecursive(line.hasOption("recursive"));
          cmd.setSubtitleSelectionDialog(line.hasOption("selection"));

          cmd.CheckUpdate();
          cmd.Run();
        } else {
          EventQueue.invokeLater(new Runnable() {
            public void run() {
              try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                MainWindow window = new MainWindow(prefctrl);
                window.setVisible(true);
              } catch (Exception e) {
                Logger.instance.error(Logger.stack2String(e));
              }
            }
          });
        }
      }
    }
  }

  public static Options getCLIOptions() {
    /**
     * CLI Options
     */
    Options options = new Options();
    options.addOption("help", false, "print this message");
    options.addOption("nogui", false, "run in cli mode");
    options.addOption("R", "recursive", false, "search every folder found in the folder");
    options.addOption("language", true,
        "gives the language to search for example: --language nl only nl or en");
    options.addOption("debug", false, "enables logging");
    options.addOption("trace", false, "more logging");
    options.addOption("importpreferences", true, "import preferences");
    options.addOption("force", false, "force to overwrite the subtitle on disk");
    options.addOption("folder", false, "folder to search");
    options.addOption("downloadall", false, "Download all found subs using '-v1' system ");
    options.addOption("updatefromonlinemapping", false, "Update with the Mappings from online");
    options.addOption("selection", false,
        "Subtitle selection possible if multiple subtitles detected");
    options.addOption("speedy", false, "speed somethings up");

    return options;
  }
}
