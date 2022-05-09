package ru.hollowhorizon.hc.client.web;

import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.OS;
import org.cef.browser.CefBrowser;
import org.cef.handler.CefAppHandlerAdapter;

public class WebManager {
    public static WebManager INSTANCE;
    private CefApp cefApp;
    private CefClient client;
    private CefBrowser browser;

    public static void init() {
//        INSTANCE = new WebManager();
//        INSTANCE.initApi();
    }

    public void initApi() {
        CefApp.startup(new String[]{});

        CefApp.addAppHandler(new CefAppHandlerAdapter(null) {
            @Override
            public void stateHasChanged(org.cef.CefApp.CefAppState state) {
                // Shutdown the app if the native CEF part is terminated
                if (state == CefApp.CefAppState.TERMINATED){
                    System.out.println("CEF ERROR");
                }
            }
        });
        CefSettings settings = new CefSettings();
        settings.windowless_rendering_enabled = OS.isLinux();
        cefApp = CefApp.getInstance(settings);
        client = cefApp.createClient();

        browser = client.createBrowser("https://www.google.com", OS.isLinux(), false);

        System.out.println("браузер загружен: "+browser);
    }

    public void stop() {
        CefApp.getInstance().dispose();
    }
}
