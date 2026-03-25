package com.uniconnect.scheduling.config;

import com.uniconnect.scheduling.model.MeetingProviderType;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "uniconnect.scheduling")
public class SchedulingProperties {

    private final Meeting meeting = new Meeting();

    public Meeting getMeeting() {
        return meeting;
    }

    public static class Meeting {
        private MeetingProviderType defaultProvider = MeetingProviderType.INTERNAL_DEFAULT;
        private String baseJoinUrl = "http://localhost:8080/meet";
        private String jitsiBaseUrl = "https://meet.jit.si";
        private String jitsiRoomPrefix = "uniconnect";
        private int minimumDurationMinutes = 15;
        private int reminderLeadMinutes = 30;

        public MeetingProviderType getDefaultProvider() {
            return defaultProvider;
        }

        public void setDefaultProvider(MeetingProviderType defaultProvider) {
            this.defaultProvider = defaultProvider;
        }

        public String getBaseJoinUrl() {
            return baseJoinUrl;
        }

        public void setBaseJoinUrl(String baseJoinUrl) {
            this.baseJoinUrl = baseJoinUrl;
        }

        public String getJitsiBaseUrl() {
            return jitsiBaseUrl;
        }

        public void setJitsiBaseUrl(String jitsiBaseUrl) {
            this.jitsiBaseUrl = jitsiBaseUrl;
        }

        public String getJitsiRoomPrefix() {
            return jitsiRoomPrefix;
        }

        public void setJitsiRoomPrefix(String jitsiRoomPrefix) {
            this.jitsiRoomPrefix = jitsiRoomPrefix;
        }

        public int getMinimumDurationMinutes() {
            return minimumDurationMinutes;
        }

        public void setMinimumDurationMinutes(int minimumDurationMinutes) {
            this.minimumDurationMinutes = minimumDurationMinutes;
        }

        public int getReminderLeadMinutes() {
            return reminderLeadMinutes;
        }

        public void setReminderLeadMinutes(int reminderLeadMinutes) {
            this.reminderLeadMinutes = reminderLeadMinutes;
        }
    }
}

