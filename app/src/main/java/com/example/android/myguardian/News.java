package com.example.android.myguardian;

/**
 * Class representing the News queried through Guardian search API
 */

class News {
    private final String sectionName;
    private final String webPublicationDate;
    private final String webTitle;
    private final String webUrl;

    News(String sectionName, String webPublicationDate, String webTitle, String webUrl) {
        this.sectionName = sectionName;
        this.webPublicationDate = webPublicationDate;
        this.webTitle = webTitle;
        this.webUrl = webUrl;
    }

    String getSectionName() {
        return sectionName;
    }

    String getWebPublicationDate() {
        return webPublicationDate;
    }

    String getWebTitle() {
        return webTitle;
    }

    String getWebUrl() {
        return webUrl;
    }
}
