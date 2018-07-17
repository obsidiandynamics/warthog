package com.obsidiandynamics.warthog;

import static org.fusesource.jansi.Ansi.*;

import java.io.*;

final class WarthogBanner {
  private WarthogBanner() {}

  static void print(PrintStream out) {
    out.println(ansi().fgBrightBlack().bold());
    // generated with http://patorjk.com/software/taag/#p=display&h=1&v=0&f=ANSI%20Shadow&t=warthog
    out.println();
    out.println(" ██╗    ██╗ █████╗ ██████╗ ████████╗██╗  ██╗ ██████╗  ██████╗ ");
    out.println(" ██║    ██║██╔══██╗██╔══██╗╚══██╔══╝██║  ██║██╔═══██╗██╔════╝ ");
    out.println(" ██║ █╗ ██║███████║██████╔╝   ██║   ███████║██║   ██║██║  ███╗");
    out.println(" ██║███╗██║██╔══██║██╔══██╗   ██║   ██╔══██║██║   ██║██║   ██║");
    out.println(" ╚███╔███╔╝██║  ██║██║  ██║   ██║   ██║  ██║╚██████╔╝╚██████╔╝");
    out.println("  ╚══╝╚══╝ ╚═╝  ╚═╝╚═╝  ╚═╝   ╚═╝   ╚═╝  ╚═╝ ╚═════╝  ╚═════╝ "); 
    out.println(ansi().reset());
  }
}
