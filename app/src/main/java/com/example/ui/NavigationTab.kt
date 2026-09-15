package com.example.ui

enum class NavigationTab(val label: String) {
    EDITOR("Editor"),
    TEMPLATES("Templates"),
    PREVIEW("Preview"),
    LOCALHOST("Localhost"),
    DEPLOY("Dist & Deploy")
}

enum class ViewportMode(val label: String, val widthDp: Int?) {
    MOBILE("Mobile (375px)", 375),
    TABLET("Tablet (768px)", 768),
    DESKTOP("Desktop (Full)", null)
}
