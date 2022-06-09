package com.petprojects.sub.checker;

import android.content.Context;

import com.petprojects.sub.PurchaseHelper;
import com.petprojects.sub.SubAppDelegate;
import com.petprojects.sub.SubConfigPrefs;
import com.petprojects.sub.SubScreenManager;
import com.petprojects.sub.checker.model.AppOpen;
import com.petprojects.sub.checker.model.EventsItem;
import com.petprojects.sub.checker.model.MessageItem;
import com.petprojects.sub.checker.model.Script;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SubAppChecker {

    private static SubAppChecker instance;
    private List<MessageItem> defaultNotificationMessages;
    private SubCheckerDelegate delegate;
    private SubAppDelegate appDelegate;

    public static SubAppChecker instance() {
        if (instance == null) {
            instance = new SubAppChecker();
        }
        return instance;
    }


    public SubAppChecker setAppDelegate(SubAppDelegate appDelegate) {
        this.appDelegate = appDelegate;
        return this;
    }

    public SubAppDelegate getAppDelegate() {
        if (appDelegate == null) {
            return new SubAppDelegate() {
                @Override
                public void registerNotificationTopic(String topic) {
                }

                @Override
                public void unregisterNotificationTopic(String topic) {
                }

                @Override
                public void logEvent(String event) {
                }
            };
        }
        return appDelegate;
    }

    public SubAppChecker setDelegate(SubCheckerDelegate delegate) {
        this.delegate = delegate;
        return this;
    }

    public SubCheckerDelegate getDelegate() {
        if (delegate == null) {
            delegate = new SubCheckerDelegate() {
                @Override
                public void pushNotification(Context context, MessageItem message) {
                }
            };
        }
        return delegate;
    }

    public SubAppChecker setDefaultNotificationMessage(MessageItem... defaultNotificationMessages) {
        this.defaultNotificationMessages = Arrays.asList(defaultNotificationMessages);
        return this;
    }

    public List<MessageItem> getDefaultNotificationMessages() {
        if (defaultNotificationMessages == null) {
            return Collections.emptyList();
        }
        return defaultNotificationMessages;
    }

    public boolean isEnableSubScript(Context context) {
        return SubConfigPrefs.get().isEnableSubScript() &&
                !PurchaseHelper.getInstance().isRemovedAds(context);
    }

    public void fromOpenAppEvent(Context context) {
        if (!isEnableSubScript(context)) {
            return;
        }
        int countOpenInLocal = SubCheckerData.instance(context).countOpen();
        Script script = getScript();
        AppOpen appOpen = script.getLifecycle().getAppOpen();
        if (appOpen == null) {
            return;
        }
        int numberOfOpenAppConfigs = appOpen.getNumber();
        if (numberOfOpenAppConfigs > 0 && countOpenInLocal >= numberOfOpenAppConfigs) {
            logEvent("SUB_NOTIFICATION_CH_SALE_OPEN");
            pushRandomNotification(context);
            SubCheckerData.instance(context).clearOpenLog();
        }
    }

    private void logEvent(String event) {
        try {
            SubAppChecker.instance().getAppDelegate().logEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Script getScript() {
        return SubConfigPrefs.get().getScriptData().getScript();
    }

    public void fromAppLoggerEvent(Context context, String event) {
        if (!isEnableSubScript(context)) {
            return;
        }
        List<EventsItem> events = getScript().getEvents();
        if (events == null) {
            return;
        }
        for (EventsItem eventsItem : events) {
            if (eventsItem.getName().equals(event)) {
                logEvent("SUB_NOTIFICATION_CH_SALE_EVENT");
                pushRandomNotification(context);
                return;
            }
        }
    }

    public void pushRandomNotification(Context context) {
        if (!isEnableSubScript(context)) {
            return;
        }
        try {
            List<MessageItem> messageItems = SubConfigPrefs.get().getScriptData().getScript().getMessage();
            if (messageItems == null || messageItems.isEmpty()) {
                messageItems = getDefaultNotificationMessages();
            }
            int size = messageItems.size();
            int randomIndex = getRandomNumber(0, size - 1);
            MessageItem defaultNotificationMessage = messageItems.get(randomIndex);
            getDelegate().pushNotification(context, defaultNotificationMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getRandomNumber(int min, int max) {
        return (new Random()).nextInt((max - min) + 1) + min;
    }
}
