package com.example.android.myguardian;

/**
 * This class demonstrates the Article which is received with the help of the Guardian search API
 */

class Article {
    private final String sectionName;
    private final String webPublicationDate;
    private final String webTitle;
    private final String webUrl;

    Article(String sectionName, String webPublicationDate, String webTitle, String webUrl) {
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
