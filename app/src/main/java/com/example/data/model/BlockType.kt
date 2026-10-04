package com.example.data.model

enum class BlockType(
    val displayName: String,
    val category: String,
    val description: String
) {
    NAVBAR(
        displayName = "Navigation Bar",
        category = "Header",
        description = "Responsive top nav with logo branding and navigation links"
    ),
    HERO(
        displayName = "Hero Banner",
        category = "Header",
        description = "Impactful headline, subtitle, action buttons, and visual graphics"
    ),
    FEATURES(
        displayName = "Features Grid",
        category = "Content",
        description = "Key benefits and highlights arranged in a modern responsive grid"
    ),
    ABOUT(
        displayName = "About / Story",
        category = "Content",
        description = "Introduce your mission, company, or personal backstory with stats"
    ),
    SERVICES(
        displayName = "Services & Offerings",
        category = "Content",
        description = "Showcase your consulting, design, or professional packages"
    ),
    TESTIMONIALS(
        displayName = "Social Proof & Reviews",
        category = "Social",
        description = "Verified customer quotes, star ratings, and client endorsements"
    ),
    PRICING(
        displayName = "Pricing Tiers",
        category = "Commerce",
        description = "Clear pricing packages with features list and checkout button"
    ),
    GALLERY(
        displayName = "Photo Gallery",
        category = "Media",
        description = "Image showcases and portfolio visuals with responsive masonry"
    ),
    CTA(
        displayName = "Call To Action Banner",
        category = "Conversion",
        description = "High-contrast conversion section with bold prompt and button"
    ),
    CONTACT(
        displayName = "Contact Form & Info",
        category = "Conversion",
        description = "Interactive inquiry submission form and contact details"
    ),
    FAQ(
        displayName = "FAQ Accordion",
        category = "Content",
        description = "Expandable questions and answers addressing customer inquiries"
    ),
    STATS(
        displayName = "Impact Stats & Numbers",
        category = "Social",
        description = "Large glowing metrics, uptime counters, and key statistical milestones"
    ),
    TIMELINE(
        displayName = "Process & Roadmap",
        category = "Content",
        description = "Step-by-step workflow, chronological milestones, or project roadmap"
    ),
    TEAM(
        displayName = "Team & Leadership",
        category = "Social",
        description = "Founder and team member profiles with avatar initials, roles, and bios"
    ),
    LOGOS(
        displayName = "Client & Partner Logos",
        category = "Social",
        description = "Curated logo cloud showing trusted partner and client brand badges"
    ),
    NEWSLETTER(
        displayName = "Newsletter Signup",
        category = "Conversion",
        description = "Email subscriber capture box with privacy reassurance"
    ),
    CUSTOM_HTML(
        displayName = "Custom HTML / Embed",
        category = "Advanced",
        description = "Raw HTML, Tailwind classes, or embeddable iframe widgets"
    ),
    WHATSAPP_SHOP(
        displayName = "WhatsApp Shop & Multi-Step Checkout",
        category = "Commerce",
        description = "Interactive e-commerce catalog, cart drawer, photo upload, 3-step checkout and WhatsApp order dispatch"
    ),
    MULTISTEP_WIZARD(
        displayName = "Multi-Step Booking & Quote Wizard",
        category = "Conversion",
        description = "Interactive 4-step wizard with selection cards, custom options, photo upload, price estimator, and WhatsApp dispatch"
    ),
    COUNTDOWN_TIMER(
        displayName = "Launch Countdown Timer",
        category = "Conversion",
        description = "Live JavaScript ticking countdown with days, hours, minutes, and seconds cards"
    ),
    IMAGE_CAROUSEL(
        displayName = "Interactive Carousel / Slider",
        category = "Media",
        description = "Touch-swipeable and arrow-navigated image carousel with caption cards and indicators"
    ),
    FOOTER(
        displayName = "Footer",
        category = "Footer",
        description = "Copyright notice, secondary links, and social channel handles"
    )
}
