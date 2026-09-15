package com.example.generator

import com.example.data.model.BlockType
import com.example.data.model.WebBlockEntity
import com.example.data.model.WebsiteEntity
import java.util.UUID

data class TemplateDefinition(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val themePreset: String,
    val fontFamily: String,
    val badge: String,
    val createWebsite: () -> WebsiteEntity,
    val createBlocks: (websiteId: Long) -> List<WebBlockEntity>
)

object WebsiteTemplates {

    val allTemplates: List<TemplateDefinition> = listOf(
        TemplateDefinition(
            id = "saas_launch",
            name = "SaaS Cloud Platform",
            description = "High-converting tech landing page with hero, interactive feature cards, pricing tiers, and social proof.",
            category = "Startup & Tech",
            themePreset = "modern-dark",
            fontFamily = "Inter, sans-serif",
            badge = "Popular",
            createWebsite = {
                WebsiteEntity(
                    title = "FlowScale Cloud",
                    slug = "flowscale-cloud",
                    description = "Modern data infrastructure for agile engineering teams.",
                    themePreset = "modern-dark",
                    fontFamily = "Inter, sans-serif"
                )
            },
            createBlocks = { webId ->
                listOf(
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 0,
                        type = BlockType.NAVBAR,
                        title = "FlowScale",
                        content = "Features|Metrics|Pricing|Community",
                        buttonText = "Start Free",
                        buttonUrl = "#pricing"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.HERO,
                        title = "Modern Data Infrastructure For Rapid Scale",
                        subtitle = "Build, deploy, and monitor reactive cloud services with zero configuration and sub-millisecond global caching.",
                        buttonText = "Deploy in 60 Seconds",
                        buttonUrl = "#pricing",
                        secondaryButtonText = "Live Documentation",
                        secondaryButtonUrl = "#features",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.LOGOS,
                        title = "TRUSTED BY ENGINEERING LEADERS GLOBALLY",
                        subtitle = "Over 10,000 teams rely on FlowScale every day",
                        content = "Google Cloud | Stripe | Vercel | Supabase | GitHub | Docker | Cloudflare"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.FEATURES,
                        title = "Engineered for Extreme Performance",
                        subtitle = "Everything you need to deliver production workloads worldwide.",
                        content = "⚡ Global Edge Cache: Sub-millisecond TTFB cached across 280+ POPs.|🔒 Zero-Trust Security: Automated mTLS, encrypted secrets, and isolated sandboxes.|📊 Real-Time Telemetry: Granular APM traces and live traffic flow visualizers."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.STATS,
                        title = "Proven Reliability at Global Scale",
                        subtitle = "Real-world infrastructure metrics measured across all 280+ edge POPs.",
                        content = "99.99%: Edge Availability SLA | < 1ms: Global Cache TTFB | 10k+: Fast-Growing Teams | 250M+: Daily Production Requests",
                        buttonText = "Explore SLA Guarantee",
                        buttonUrl = "#pricing"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.TESTIMONIALS,
                        title = "Trusted by Fast-Growing Engineering Teams",
                        content = "\"FlowScale slashed our global cloud latency by 74% within 48 hours of migration. The developer ergonomics are unmatched.\"",
                        subtitle = "Sarah Jenkins — VP of Engineering at TechVanguard",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 6,
                        type = BlockType.PRICING,
                        title = "Transparent & Scalable Pricing",
                        subtitle = "No hidden fees. Scale automatically as your userbase grows.",
                        content = "Starter ($0/mo): 100k requests, Community Support, Standard Edge|Pro ($49/mo): 5M requests, Custom Domains, 99.99% SLA, 24/7 Priority Support|Enterprise ($199/mo): Unlimited scaling, Dedicated VPC, Custom SLA, SSO SAML",
                        buttonText = "Choose Plan",
                        buttonUrl = "#contact"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 7,
                        type = BlockType.FAQ,
                        title = "Frequently Asked Questions",
                        subtitle = "Everything you need to know about our cloud architecture and pricing.",
                        content = "How fast is deployment?:Your project deploys across our global edge network in under 60 seconds with automated SSL.|Can I bring my own custom domain?:Yes! All tiers include full support for custom root domains and subdomains with automatic DNS verification.|What happens if I exceed plan limits?:We automatically scale your capacity without throttling or sudden outages, alerting you beforehand.|Is there a money-back guarantee?:We offer a 30-day no-questions-asked refund policy on all paid subscription plans."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 8,
                        type = BlockType.CONTACT,
                        title = "Get In Touch With Engineering",
                        subtitle = "Have questions about custom enterprise deployments or need dedicated support?",
                        content = "Our core engineering team is available 24/7. Reach out directly or submit your inquiry below.",
                        buttonText = "Send Message",
                        buttonUrl = "#contact"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 9,
                        type = BlockType.CTA,
                        title = "Ready to Supercharge Your Infrastructure?",
                        subtitle = "Join thousands of builders already shipping production apps faster.",
                        buttonText = "Get Started For Free",
                        buttonUrl = "#pricing",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 10,
                        type = BlockType.FOOTER,
                        title = "FlowScale Inc.",
                        content = "© 2026 FlowScale. Built with Web Builder. Open Source & Community Powered.",
                        subtitle = "GitHub • Twitter • Discord • Docs"
                    )
                )
            }
        ),

        TemplateDefinition(
            id = "creative_portfolio",
            name = "Creative Studio & Portfolio",
            description = "Sleek, minimalist design portfolio showcasing work, design philosophy, client testimonials, and inquiry form.",
            category = "Creative",
            themePreset = "cyber-neon",
            fontFamily = "Playfair Display, serif",
            badge = "Designers",
            createWebsite = {
                WebsiteEntity(
                    title = "Elena Vance — Creative Director",
                    slug = "elena-vance-design",
                    description = "Digital art, branding identities, and human-centric design.",
                    themePreset = "cyber-neon",
                    fontFamily = "Playfair Display, serif"
                )
            },
            createBlocks = { webId ->
                listOf(
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 0,
                        type = BlockType.NAVBAR,
                        title = "ELENA VANCE",
                        content = "Work|Philosophy|Services|Contact",
                        buttonText = "Let's Talk",
                        buttonUrl = "#contact"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.HERO,
                        title = "Crafting Digital Artifacts That Inspire Wonder",
                        subtitle = "Independent design director specializing in bold interactive identities, editorial typography, and immersive web experiences.",
                        buttonText = "Explore Selected Works",
                        buttonUrl = "#services",
                        alignment = "left"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.ABOUT,
                        title = "About The Practice",
                        subtitle = "10+ Years of Craft in Tokyo & New York",
                        content = "Believing that technology should feel tactile and memorable. Partnering with visionary founders, luxury brands, and cultural institutions to shape products that leave lasting impressions."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.SERVICES,
                        title = "Disciplines & Offerings",
                        subtitle = "From initial sketch to high-fidelity deployment",
                        content = "🎨 Visual Brand Identity: Logomarks, art direction, and design guidelines.|📱 Next-Gen UI/UX: Interactive web & native mobile application design.|✨ Motion & 3D Art: Kinetic typography and promotional video assets."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.CONTACT,
                        title = "Let's Build Something Exceptional",
                        subtitle = "Currently taking select commissions for Q3/Q4.",
                        content = "Send project inquiries to elena@vancestudio.design",
                        buttonText = "Send Message",
                        buttonUrl = "mailto:elena@vancestudio.design"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.FOOTER,
                        title = "Elena Vance Studio",
                        content = "© 2026 Elena Vance. All rights reserved. Designed with Web Builder.",
                        subtitle = "Dribbble • Behance • Instagram • LinkedIn"
                    )
                )
            }
        ),

        TemplateDefinition(
            id = "artisan_cafe",
            name = "Artisan Cafe & Bakery",
            description = "Warm and welcoming restaurant template with menu highlights, roasting philosophy, location, and orders.",
            category = "Hospitality",
            themePreset = "sunset-warm",
            fontFamily = "Georgia, serif",
            badge = "Warm",
            createWebsite = {
                WebsiteEntity(
                    title = "Velvet & Grain Roasters",
                    slug = "velvet-and-grain",
                    description = "Single-origin pour overs and fresh hearth-baked breads.",
                    themePreset = "sunset-warm",
                    fontFamily = "Georgia, serif"
                )
            },
            createBlocks = { webId ->
                listOf(
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 0,
                        type = BlockType.NAVBAR,
                        title = "Velvet & Grain",
                        content = "Story|Roastery|Menu|Location",
                        buttonText = "Order Ahead",
                        buttonUrl = "#menu"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.HERO,
                        title = "Single-Origin Roasts & Hearth-Baked Goodness",
                        subtitle = "Slow down and savor ethically sourced coffees, wild sourdoughs, and butter croissants crafted with care every dawn.",
                        buttonText = "Explore Seasonal Menu",
                        buttonUrl = "#menu",
                        secondaryButtonText = "Our Story",
                        secondaryButtonUrl = "#about",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.FEATURES,
                        title = "The Craft Behind Every Cup",
                        subtitle = "From small farmer cooperatives straight to your ceramic mug.",
                        content = "🌱 Single-Origin Direct Trade: Transparent relationships with family farms in Oaxaca and Yirgacheffe.|🔥 Micro-Batch Roasting: Roasted in small 5kg cast iron drums to highlight floral nuances.|🥐 Fermented Sourdoughs: Naturally leavened sourdough baguettes baked fresh every morning at 5 AM."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.TESTIMONIALS,
                        title = "Loved By Our Community",
                        content = "\"The best cardamom bun and cleanest Ethiopian natural pour-over in the Pacific Northwest. An absolute sanctuary on rainy mornings.\"",
                        subtitle = "Eater Magazine • 2026 Cafe of the Year",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.CONTACT,
                        title = "Visit The Roastery",
                        subtitle = "428 Pine Street, Portland, Oregon",
                        content = "Open Daily: 7:00 AM – 5:00 PM | Weekend Brunch: 8:00 AM – 3:00 PM",
                        buttonText = "Get Directions",
                        buttonUrl = "https://maps.google.com"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.FOOTER,
                        title = "Velvet & Grain Roasters",
                        content = "© 2026 Velvet & Grain Roasters. Proudly local & sustainable.",
                        subtitle = "Instagram • Yelp • Wholesale Inquiries"
                    )
                )
            }
        ),

        TemplateDefinition(
            id = "link_in_bio",
            name = "Personal Brand & Link Hub",
            description = "Mobile-first link-in-bio page perfect for content creators, authors, podcasters, and influencers.",
            category = "Creator",
            themePreset = "clean-light",
            fontFamily = "Inter, sans-serif",
            badge = "Mobile-First",
            createWebsite = {
                WebsiteEntity(
                    title = "Alex Rivera — Link Hub",
                    slug = "alex-rivera-links",
                    description = "Tech creator, developer advocate, and weekly newsletter host.",
                    themePreset = "clean-light",
                    fontFamily = "Inter, sans-serif"
                )
            },
            createBlocks = { webId ->
                listOf(
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 0,
                        type = BlockType.HERO,
                        title = "Alex Rivera",
                        subtitle = "Software Engineer & Tech Creator sharing modern web dev workflows, AI tutorials, and startup building.",
                        buttonText = "Watch Latest Video",
                        buttonUrl = "https://youtube.com",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.FEATURES,
                        title = "Featured Links & Channels",
                        subtitle = "Connect with my projects across the web",
                        content = "📺 YouTube Channel: 85,000+ engineers learning modern full-stack.|💻 GitHub Projects: 40+ open source tools and starters.|📰 The CodeCraft Digest: Weekly deep dive into system architecture.|🎙️ The Full Stack Podcast: Conversations with top open-source builders."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.NEWSLETTER,
                        title = "Join 25,000+ Readers",
                        subtitle = "Get my weekly curated breakdown of web tooling and architecture breakdowns directly to your inbox.",
                        buttonText = "Subscribe Free",
                        buttonUrl = "#newsletter"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.FOOTER,
                        title = "Alex Rivera",
                        content = "© 2026 Alex Rivera. Built with Open Source Web Builder.",
                        subtitle = "YouTube • X / Twitter • GitHub • LinkedIn"
                    )
                )
            }
        ),

        TemplateDefinition(
            id = "dev_resume",
            name = "Developer Portfolio & CV",
            description = "Clean, high-credibility resume and portfolio for software engineers, showcasing tech stack and GitHub.",
            category = "Resume",
            themePreset = "emerald-minimal",
            fontFamily = "JetBrains Mono, monospace",
            badge = "Developer",
            createWebsite = {
                WebsiteEntity(
                    title = "Kaelen Thorne — Systems Engineer",
                    slug = "kaelen-thorne-cv",
                    description = "Distributed systems, Linux kernel telemetry, and Rust.",
                    themePreset = "emerald-minimal",
                    fontFamily = "JetBrains Mono, monospace"
                )
            },
            createBlocks = { webId ->
                listOf(
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 0,
                        type = BlockType.NAVBAR,
                        title = "~/kaelen.dev",
                        content = "About|Stack|Projects|Contact",
                        buttonText = "Resume (PDF)",
                        buttonUrl = "#contact"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.HERO,
                        title = "Kaelen Thorne",
                        subtitle = "Senior Systems Engineer focused on high-throughput distributed databases, consensus protocols, and Linux performance tuning.",
                        buttonText = "View GitHub Repos",
                        buttonUrl = "https://github.com",
                        secondaryButtonText = "Contact Directly",
                        secondaryButtonUrl = "#contact",
                        alignment = "left"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.FEATURES,
                        title = "Technical Competencies",
                        subtitle = "Tools and technologies deployed in production",
                        content = "🦀 Rust & Go: High-performance low-latency network engines and microservices.|☸️ Cloud & Kubernetes: Multi-region orchestrations, eBPF tracing, and automated failover.|🗄️ Distributed Storage: Raft consensus, LSM-Tree databases, and append-only logs."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.ABOUT,
                        title = "Career Highlights",
                        subtitle = "Over 8 years scaling backend architectures",
                        content = "Architected real-time ingestion pipelines processing over 250,000 events/sec at sub-5ms p99 latency. Maintainer of popular open-source benchmarking utilities."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.CONTACT,
                        title = "Get In Touch",
                        subtitle = "Available for advisory and technical consulting.",
                        content = "Email: kaelen@thorne.systems | PGP Key Available",
                        buttonText = "Send Email",
                        buttonUrl = "mailto:kaelen@thorne.systems"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.FOOTER,
                        title = "kaelen.dev",
                        content = "Terminal theme generated with Web Builder. Free & Open Source.",
                        subtitle = "GitHub • Blog • Bluesky"
                    )
                )
            }
        ),

        TemplateDefinition(
            id = "modern_blog",
            name = "Modern Blog & Editorial",
            description = "Clean, typography-focused publication template for tech writers, journalists, essayists, and thought leaders.",
            category = "Blog",
            themePreset = "clean-light",
            fontFamily = "Playfair Display, serif",
            badge = "Editorial",
            createWebsite = {
                WebsiteEntity(
                    title = "The Modern Chronicle",
                    slug = "modern-chronicle",
                    description = "Essays on design, software architecture, and technology culture.",
                    themePreset = "clean-light",
                    fontFamily = "Playfair Display, serif"
                )
            },
            createBlocks = { webId ->
                listOf(
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 0,
                        type = BlockType.NAVBAR,
                        title = "THE CHRONICLE",
                        content = "Articles|Interviews|Topics|About",
                        buttonText = "Subscribe",
                        buttonUrl = "#newsletter"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.HERO,
                        title = "Curated Essays on Technology, Design & Modern Culture",
                        subtitle = "Independent writing exploring how software architecture, typography, and human-centered design shape our world.",
                        buttonText = "Read Latest Issue",
                        buttonUrl = "#features",
                        secondaryButtonText = "About Publication",
                        secondaryButtonUrl = "#about",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.FEATURES,
                        title = "Featured Articles & Deep Dives",
                        subtitle = "Most read perspectives this month",
                        content = "🏛️ The Lost Art of Software Craft: Why simplicity is the ultimate scalability strategy.|📐 Typography in the AI Era: How tactile editorial layouts restore human connection.|🌐 Edge Computing & The Open Web: Decentralized hosting models transforming the internet."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.NEWSLETTER,
                        title = "Never Miss a New Edition",
                        subtitle = "Delivered directly to your inbox every Sunday morning. No spam, just high-signal essays.",
                        buttonText = "Join 20,000+ Readers",
                        buttonUrl = "#newsletter"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.ABOUT,
                        title = "About The Publication",
                        subtitle = "Independent Editorial Collective",
                        content = "The Modern Chronicle was founded to champion slow, deliberate technical journalism and design criticism. Every piece is written by practitioners and community contributors."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.FOOTER,
                        title = "The Modern Chronicle",
                        content = "© 2026 The Modern Chronicle. Powered by Open-Source Web Builder.",
                        subtitle = "RSS Feed • Mastodon • Twitter • Newsletter Archive"
                    )
                )
            }
        ),

        TemplateDefinition(
            id = "agency_studio",
            name = "Nexus Digital Agency & Studio",
            description = "High-impact digital agency template featuring partner logos, step-by-step process roadmap, team bios, and verified metrics.",
            category = "Agency & Studio",
            themePreset = "cyber-neon",
            fontFamily = "Inter, sans-serif",
            badge = "Featured",
            createWebsite = {
                WebsiteEntity(
                    title = "Nexus Studio & Labs",
                    slug = "nexus-agency-labs",
                    description = "We design and engineer high-impact digital experiences for industry leaders.",
                    themePreset = "cyber-neon",
                    fontFamily = "Inter, sans-serif"
                )
            },
            createBlocks = { webId ->
                listOf(
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 0,
                        type = BlockType.NAVBAR,
                        title = "NEXUS // LABS",
                        content = "Work|Process|Team|Impact|Contact",
                        buttonText = "Start Project",
                        buttonUrl = "#contact"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.HERO,
                        title = "We Engineer Digital Realities for Tomorrow",
                        subtitle = "Boutique creative engineering studio partnering with pioneering founders to design iconic brand identities, reactive web apps, and immersive AI interfaces.",
                        buttonText = "Explore Our Works",
                        buttonUrl = "#process",
                        secondaryButtonText = "Schedule Discovery Call",
                        secondaryButtonUrl = "#contact",
                        alignment = "left"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.LOGOS,
                        title = "TRUSTED BY PIONEERING ENTERPRISES",
                        subtitle = "Proud to collaborate with visionary teams across 14 countries",
                        content = "OpenAI | Stripe | Figma | Linear | Vercel | Notion | Apple"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.TIMELINE,
                        title = "Our Execution Framework",
                        subtitle = "From whiteboarding vision to shipping high-fidelity production systems in weeks.",
                        content = "01 Discovery & Strategy: Uncovering core differentiators, user mental models, and market positioning.|02 Creative Prototyping: Interactive tactile prototypes, motion design, and system architecture.|03 Production Engineering: Pixel-perfect frontend builds, zero-latency deployment, and automated scaling."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.STATS,
                        title = "Engineered For Measurable Impact",
                        subtitle = "Key benchmarks delivered across our recent global client launches.",
                        content = "4.8x: Average Conversion Lift | 98/100: Google Lighthouse Performance | $140M+: Client Capital Raised | 12: International Design Awards",
                        buttonText = "Read Case Studies",
                        buttonUrl = "#contact"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.TEAM,
                        title = "Creative & Engineering Leadership",
                        subtitle = "A compact multidisciplinary team of senior practitioners.",
                        content = "Maya Sterling: Creative Director: Former design lead at Pentagram & Apple Design Lab.|Leo Zhang: Head of Systems: Distributed systems architect and WebGL contributor.|Amara O'Connor: Product Strategist: Ex-Stripe lead focused on conversion & retention."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 6,
                        type = BlockType.CTA,
                        title = "Have a Landmark Project in Mind?",
                        subtitle = "We take on 4 marquee client partnerships per quarter. Let's explore your timeline.",
                        buttonText = "Schedule Discovery Call",
                        buttonUrl = "mailto:hello@nexuslabs.design"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 7,
                        type = BlockType.FOOTER,
                        title = "Nexus Studio & Labs",
                        content = "© 2026 Nexus Studio. Operating from San Francisco, London & Tokyo.",
                        subtitle = "X / Twitter • GitHub • Dribbble • LinkedIn"
                    )
                )
            }
        ),

        TemplateDefinition(
            id = "ecommerce_store",
            name = "Aura Minimalist Goods & Store",
            description = "E-Commerce brand storefront showcasing hero product spotlight, curated collections, customer reviews, and fast checkout call-to-actions.",
            category = "E-Commerce",
            themePreset = "clean-light",
            fontFamily = "Inter, sans-serif",
            badge = "E-Commerce",
            createWebsite = {
                WebsiteEntity(
                    title = "AURA Goods — Living Made Pure",
                    slug = "aura-minimal-goods",
                    description = "Sustainable everyday essentials thoughtfully crafted from organic materials.",
                    themePreset = "clean-light",
                    fontFamily = "Inter, sans-serif"
                )
            },
            createBlocks = { webId ->
                listOf(
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 0,
                        type = BlockType.NAVBAR,
                        title = "AURA GOODS",
                        content = "Shop All|Ceramics|Textiles|About|Journal",
                        buttonText = "Bag (0)",
                        buttonUrl = "#shop"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.HERO,
                        title = "Everyday Objects Made With Reverence",
                        subtitle = "Timeless ceramics, stone-washed linens, and botanical fragrances designed to bring calm and tactile joy to your domestic rituals.",
                        buttonText = "Shop New Arrivals",
                        buttonUrl = "#shop",
                        secondaryButtonText = "Read Our Sourcing Story",
                        secondaryButtonUrl = "#about",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.FEATURES,
                        title = "Why Conscious Living Chooses AURA",
                        subtitle = "Honest materials and transparent ethical craftsmanship in every detail.",
                        content = "🌿 100% Organic & Recycled: Zero synthetic dyes, biodegradable plant-based packaging.|🤲 Artisan Handcrafted: Small studio runs crafted by master ceramists in Portugal and Japan.|🚚 Carbon-Neutral Shipping: Every order offset through verified reforestation reserves."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.STATS,
                        title = "Our Sustainability Footprint",
                        subtitle = "Measurable commitment to ethical manufacturing and fair living wages.",
                        content = "100%: Plastic-Free Packaging | 32,000+: Happy Conscious Homes | 4,200+: Trees Planted in 2026 | 0%: Animal Byproducts"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.TESTIMONIALS,
                        title = "Words From Our Community",
                        content = "\"The heavyweight waffle throw and matcha ceramic cup are sublime. They have completely elevated my morning ritual.\"",
                        subtitle = "Camille Laurent — Architectural Digest Reviewer",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.FAQ,
                        title = "Orders & Care Questions",
                        subtitle = "Helpful information regarding shipping, gift packaging, and product care.",
                        content = "How do I care for stoneware?: Hand wash recommended, microwave and dishwasher safe up to 250°C.|Where do you ship?: We ship carbon-neutral worldwide with tracking provided within 24 hours.|What is your return policy?: Enjoy 30 days of hassle-free returns on all domestic purchases."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 6,
                        type = BlockType.CTA,
                        title = "Enjoy 15% Off Your First Order",
                        subtitle = "Join our journal for seasonal releases, slow design essays, and exclusive archive sales.",
                        buttonText = "Claim 15% Code",
                        buttonUrl = "#contact"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 7,
                        type = BlockType.FOOTER,
                        title = "AURA Goods Co.",
                        content = "© 2026 AURA Goods Co. Crafted with care for slow living.",
                        subtitle = "Instagram • Pinterest • Fair Trade Certified"
                    )
                )
            }
        ),

        TemplateDefinition(
            id = "blank_canvas",
            name = "Blank Canvas (From Scratch)",
            description = "Start with a clean slate. Build your custom layout block by block with complete creative freedom.",
            category = "Custom",
            themePreset = "modern-dark",
            fontFamily = "Inter, sans-serif",
            badge = "Blank",
            createWebsite = {
                WebsiteEntity(
                    title = "My Custom Website",
                    slug = "my-custom-website",
                    description = "Custom responsive site created with Web Builder.",
                    themePreset = "modern-dark",
                    fontFamily = "Inter, sans-serif"
                )
            },
            createBlocks = { webId ->
                listOf(
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 0,
                        type = BlockType.NAVBAR,
                        title = "My Site",
                        content = "Home|About|Contact",
                        buttonText = "Contact",
                        buttonUrl = "#contact"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.HERO,
                        title = "Design Your Custom Website",
                        subtitle = "Tap 'Add Section' to drag and configure responsive components.",
                        buttonText = "Get Started",
                        buttonUrl = "#contact",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.FOOTER,
                        title = "My Site",
                        content = "© 2026 My Site. Built with Web Builder."
                    )
                )
            }
        )
    )

    fun getDefaultTemplate(): TemplateDefinition = allTemplates.first()
}
