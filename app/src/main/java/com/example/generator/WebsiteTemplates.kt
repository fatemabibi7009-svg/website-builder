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
    val isPremier: Boolean = false,
    val createWebsite: () -> WebsiteEntity,
    val createBlocks: (websiteId: Long) -> List<WebBlockEntity>
)

object WebsiteTemplates {

    fun is4BitAnime(template: TemplateDefinition): Boolean {
        return template.id.contains("4bit") ||
                template.id.contains("anime") ||
                template.category.contains("Anime", ignoreCase = true) ||
                template.category.contains("4-Bit", ignoreCase = true) ||
                template.themePreset.contains("4bit") ||
                template.themePreset.contains("gameboy") ||
                template.themePreset.contains("arcade")
    }

    val allTemplates: List<TemplateDefinition> = listOf(
        TemplateDefinition(
            id = "premier_whatsapp_shop",
            name = "Knot & Weave WhatsApp Store",
            description = "Multi-step shopping experience with category filters, interactive cart drawer, custom photo upload, 3-step checkout wizard, and WhatsApp order dispatch.",
            category = "⭐ Premier",
            themePreset = "minimal-light",
            fontFamily = "Inter, sans-serif",
            badge = "PREMIER PRO",
            isPremier = true,
            createWebsite = {
                WebsiteEntity(
                    title = "Knot & Weave",
                    slug = "knot-and-weave",
                    description = "Handcrafted artisan bags with instant WhatsApp ordering and custom photo upload.",
                    themePreset = "minimal-light",
                    fontFamily = "Inter, sans-serif"
                )
            },
            createBlocks = { webId ->
                listOf(
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 0,
                        type = BlockType.NAVBAR,
                        title = "Knot & Weave",
                        content = "Shop|Process|Custom Order|FAQ",
                        buttonText = "WhatsApp Order",
                        buttonUrl = "https://wa.me/919876543210"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.WHATSAPP_SHOP,
                        title = "Knot & Weave",
                        subtitle = "Artisan Festival • Up to 50% off select handcrafted bags",
                        content = "Lavender Breeze Tote: 1249: 2499: 4.9: Handwoven pastel purple merino wool tote: https://images.unsplash.com/photo-1544816155-12df9643f363?w=500&q=80: Totes | Crimson Night Crossbody: 899: 1599: 4.5: Crocheted crossbody with leather strap: https://images.unsplash.com/photo-1584917865442-de89df76afd3?w=500&q=80: Crossbody | Earthen Clay Handbag: 1499: 2999: 4.8: Premium terracotta merino wool handbag: https://images.unsplash.com/photo-1590874103328-eac38a683ce7?w=500&q=80: Handbags | Saffron Bloom Wallet: 499: 999: 4.7: Compact hand-stitched artisan card wallet: https://images.unsplash.com/photo-1627123424574-724758594e93?w=500&q=80: Wallets | Indigo Wave Oversized Tote: 1599: 3199: 5.0: Roomy artisan beach & market wool tote: https://images.unsplash.com/photo-1575032617751-6ddec2089882?w=500&q=80: Totes",
                        buttonText = "Order on WhatsApp",
                        buttonUrl = "919876543210",
                        imageUrl = "https://images.unsplash.com/photo-1544816155-12df9643f363?w=800&q=80",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.ABOUT,
                        title = "Handcrafted with Care",
                        subtitle = "100% Sustainable & Handwoven",
                        content = "Every Knot & Weave bag is intricately handwoven by rural master artisans using pure organic merino wool and vegetable dyes. When you order, your bag is custom packaged with a signed artisan note.",
                        buttonText = "Learn Our Process",
                        buttonUrl = "#process"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.TIMELINE,
                        title = "From Raw Wool to Artisan Keepsake",
                        subtitle = "Our transparent 4-stage hand-crafting process",
                        content = "01 Ethical Shearing & Sorting: We source 100% cruelty-free organic Merino fleece from high-altitude shepherd cooperatives.|02 Botanical Vegetable Dyeing: Wool is steeped in wild madder root, marigold blossoms, and indigo leaf vats for vibrant non-toxic color.|03 Handloom & Crochet Weaving: Master artisans spend 14 to 26 hours carefully knotting and hand-weaving every distinct bag pattern.|04 Quality Mark & Wax Seal: Each piece is inspected, signed by its maker, and wax-sealed with our signature artisan heritage seal."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.FAQ,
                        title = "Shopping & Delivery FAQ",
                        content = "How does WhatsApp checkout work?: Once you finish the 3-step checkout, tap 'Send Order to WhatsApp' to message our team with your order summary and delivery address.|Can I upload a custom photo?: Yes! Use the Photo Upload tool in the store to attach reference designs or embroidery initials.|How fast is delivery?: We offer FREE express delivery across the country within 24 to 48 hours.|What payment methods are supported?: We support UPI (PhonePe, GPay, Paytm with ₹10 instant discount) and Cash on Delivery.",
                        buttonText = "Ask a Question",
                        buttonUrl = "https://wa.me/919876543210"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.FOOTER,
                        title = "Knot & Weave Handcrafted",
                        subtitle = "Instagram • WhatsApp • Pinterest",
                        content = "© 2026 Knot & Weave. Built with Web Builder."
                    )
                )
            }
        ),
        TemplateDefinition(
            id = "multistep_bakery_wizard",
            name = "Velvet & Vanilla Cake Studio",
            description = "Multi-step bespoke cake order wizard with flavor selection, tiered sizing, reference design photo upload, delivery scheduling, and WhatsApp dispatch.",
            category = "⭐ Premier",
            themePreset = "minimal-light",
            fontFamily = "Inter, sans-serif",
            badge = "MULTI-STEP",
            isPremier = true,
            createWebsite = {
                WebsiteEntity(
                    title = "Velvet & Vanilla Patisserie",
                    slug = "velvet-and-vanilla",
                    description = "Artisan bespoke cakes with instant 4-step custom quote builder and photo reference upload.",
                    themePreset = "minimal-light",
                    fontFamily = "Inter, sans-serif"
                )
            },
            createBlocks = { webId ->
                listOf(
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 0,
                        type = BlockType.NAVBAR,
                        title = "Velvet & Vanilla",
                        content = "Custom Cakes|Wizard|Flavors|Reviews|Contact",
                        buttonText = "Order on WhatsApp",
                        buttonUrl = "https://wa.me/919876543210"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.HERO,
                        title = "Bespoke Sculpted Cakes For Life's Purest Celebrations",
                        subtitle = "Select your design tier, attach custom Pinterest reference photos, and lock in your delivery slot via WhatsApp in minutes.",
                        buttonText = "Start Custom Cake Wizard",
                        buttonUrl = "#wizard",
                        secondaryButtonText = "Explore Flavors",
                        secondaryButtonUrl = "#flavors",
                        imageUrl = "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=1200&auto=format&fit=crop&q=80",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.MULTISTEP_WIZARD,
                        title = "Custom Cake & Event Wizard",
                        subtitle = "Choose your cake tier, select gourmet fillings, attach reference photos, and send directly to WhatsApp",
                        content = "Signature Birthday Tier: ₹1,499: 1.5kg double-barrel cake with buttercream stenciling & custom message: 🎂 | Deluxe Floral Luxe: ₹2,899: 2.5kg semi-naked tiered cake with fresh edible botanicals & gold leaf: 🌸 | Grand Wedding Masterpiece: ₹5,999: 3-tier architectural cake with sugar sculpture florals & tasting box: 👑 | Assorted Macaron & Petit Tower: ₹999: 24 gourmet French macarons with seasonal fruit curds & custom ribbon: 🧁",
                        buttonText = "Send Cake Order to WhatsApp",
                        buttonUrl = "919876543210",
                        imageUrl = "https://images.unsplash.com/photo-1535141192574-5d4897c13136?w=1200&auto=format&fit=crop&q=80",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.FEATURES,
                        title = "Our Signature Gourmet Flavors",
                        subtitle = "Crafted with Belgian chocolate, Tahitian vanilla beans, and organic fruit purees",
                        content = "Madagascar Bourbon Vanilla: Light chiffon layered with salted caramel drizzle and whipped mascarpone ganache.|Belgian Dark Truffle: 72% Valrhona dark chocolate crumb with roasted hazelnut praline crunch.|Wild Raspberry Pistachio: Fragrant pistachio sponge with tart raspberry coulis and rosewater buttercream.|Earl Grey & Lavender Lemon: Infused bergamot tea cake with zesty lemon curd and lavender cream."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.FAQ,
                        title = "Bespoke Order Guidelines",
                        content = "How early should I order?: For standard custom cakes, we recommend 48 hours notice. Wedding tiers require 2 weeks notice.|Can I upload my own cake reference photo?: Yes! Use Step 3 in the Wizard to attach any photo or Pinterest sketch.|Do you offer eggless and vegan options?: Yes, all our signature flavors are available in 100% eggless and vegan variations upon request.|How is the cake delivered?: Delivered in temperature-controlled vehicles with shockproof suspension boxes to ensure pristine arrival.",
                        buttonText = "Chat on WhatsApp",
                        buttonUrl = "https://wa.me/919876543210"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.FOOTER,
                        title = "Velvet & Vanilla Patisserie",
                        subtitle = "Bespoke Cakes • Express Delivery • WhatsApp Orders",
                        content = "© 2026 Velvet & Vanilla. Built with Web Builder."
                    )
                )
            }
        ),
        TemplateDefinition(
            id = "multistep_interior_design",
            name = "Lumina Interior & Renovation Wizard",
            description = "Multi-step architectural consultation and renovation quote wizard with space selector, moodboard photo upload, and instant WhatsApp booking.",
            category = "⭐ Premier",
            themePreset = "modern-dark",
            fontFamily = "Inter, sans-serif",
            badge = "MULTI-STEP",
            isPremier = true,
            createWebsite = {
                WebsiteEntity(
                    title = "Lumina Spatial Studio",
                    slug = "lumina-spatial-studio",
                    description = "Contemporary architectural interior design and turnkey renovation estimates.",
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
                        title = "Lumina Studio",
                        content = "Portfolio|Quote Wizard|Philosophy|Contact",
                        buttonText = "Book Consultation",
                        buttonUrl = "https://wa.me/919876543210"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.HERO,
                        title = "Transforming Spaces Into Sculptural Living Experiences",
                        subtitle = "Explore our turnkey interior packages, upload your floorplan or moodboard photo, and receive an itemized quote via WhatsApp in 30 minutes.",
                        buttonText = "Launch Renovation Wizard",
                        buttonUrl = "#wizard",
                        secondaryButtonText = "View Recent Works",
                        secondaryButtonUrl = "#about",
                        imageUrl = "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?w=1200&auto=format&fit=crop&q=80",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.MULTISTEP_WIZARD,
                        title = "Turnkey Interior Estimate Wizard",
                        subtitle = "Select your space configuration, specify preferred finishes, upload floorplan photos, and get instant consultation",
                        content = "Studio & Living Transformation: ₹45,000: Complete layout reimagination with modular carpentry, acoustic panels & custom lighting: 🛋️ | 2BHK Complete Turnkey: ₹1,20,000: Full home renovation including kitchen cabinetry, false ceiling & Italian stone finishes: 🏡 | Luxury Villa Architecture: ₹2,80,000: Comprehensive spatial planning, smart home automation & bespoke artisan furnishings: 🏛️ | Commercial Studio & Cafe: ₹85,000: Brand-aligned hospitality interior with custom counters, seating & lighting design: ☕",
                        buttonText = "Send Estimate Request to WhatsApp",
                        buttonUrl = "919876543210",
                        imageUrl = "https://images.unsplash.com/photo-1600210492486-724fe5c67fb0?w=1200&auto=format&fit=crop&q=80",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.STATS,
                        title = "Proven Architectural Delivery",
                        content = "140+: Completed Homes | 4.9★: Client Satisfaction | 21 Days: Rapid Execution | 10 Yr: Structural Guarantee"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.FOOTER,
                        title = "Lumina Spatial Studio",
                        subtitle = "Architecture • Interiors • Project Execution",
                        content = "© 2026 Lumina Studio. Built with Web Builder."
                    )
                )
            }
        ),

        TemplateDefinition(
            id = "premier_4bit_akiba_arcade_store",
            name = "Akiba 4-Bit Cartridge & Retro Store ~ 秋葉原",
            description = "Premier 4-bit retro gaming store with vintage cartridges, chiptune vinyls, pixel art acrylics, interactive cart drawer, 3-step checkout, and instant WhatsApp ordering.",
            category = "⭐ Premier & 4-Bit",
            themePreset = "anime-4bit",
            fontFamily = "'Press Start 2P', monospace",
            badge = "4-BIT PREMIER",
            isPremier = true,
            createWebsite = {
                WebsiteEntity(
                    title = "Akiba 4-Bit ~ 秋葉原",
                    slug = "akiba-4bit-store",
                    description = "Akihabara premier 4-bit retro game cartridge shop with instant WhatsApp order dispatch, interactive cart, and photo upload.",
                    themePreset = "anime-4bit",
                    fontFamily = "'Press Start 2P', monospace",
                    buttonStyle = "pixel-4bit",
                    buttonRadius = "sharp",
                    customPrimaryColor = "#FF2A85",
                    customBackgroundColor = "#0F051D",
                    customCss = """
.kw-product-card, .kw-cart-drawer, .kw-modal-content {
  border: 3px solid #7E22CE !important;
  box-shadow: 4px 4px 0px #000000 !important;
  border-radius: 0px !important;
  image-rendering: pixelated !important;
}
.kw-add-btn, .kw-wa-btn {
  font-family: inherit !important;
  border-radius: 0px !important;
  border: 2px solid #000000 !important;
  box-shadow: 3px 3px 0px #000000 !important;
  text-transform: uppercase !important;
}
"""
                )
            },
            createBlocks = { webId ->
                listOf(
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 0,
                        type = BlockType.NAVBAR,
                        title = "AKIBA 4-BIT 秋葉原",
                        content = "Cartridges|Vinyls|Mod Consoles|Collectibles|FAQ",
                        buttonText = "Order on WhatsApp",
                        buttonUrl = "https://wa.me/919876543210"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.WHATSAPP_SHOP,
                        title = "Akiba 4-Bit Cartridge & Hardware Armory",
                        subtitle = "Authentic restored 80s/90s Japanese cartridges, custom IPS mods & chiptunes",
                        content = "Chrono Blade 4-Bit Cartridge: 1499: 2999: 5.0: Authentically flashed physical cartridge with holographic box: https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=600&q=80: Cartridges | Neon Mecha Pixel Acrylic: 499: 999: 4.9: 4-layer laser-cut 4-bit acrylic desk companion: https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=600&q=80: Collectibles | Chiptune Symphony 12\" Vinyl: 1899: 3499: 4.8: Hand-pressed glow-in-the-dark chiptune soundtrack: https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&q=80: Vinyl | Game Boy IPS V3 Modded Console: 4999: 8999: 5.0: Custom shell with OSD backlight and rechargeable USB-C battery: https://images.unsplash.com/photo-1531525645387-7f14be1bdbbd?w=600&q=80: Consoles | Pixel Waifu Holographic Keychain: 299: 599: 4.7: Double-sided 4-bit acrylic keychain with glitter finish: https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600&q=80: Collectibles",
                        buttonText = "Send Order to WhatsApp",
                        buttonUrl = "919876543210",
                        imageUrl = "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=1200&q=80",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.ABOUT,
                        title = "Hand-Crafted in Tokyo Electric Town",
                        subtitle = "Preserving the Pure 4-Bit Golden Era",
                        content = "Every cartridge sold by Akiba 4-Bit is ultrasonically cleaned, verified on original test boards, and sealed in archival protective sleeves. Custom console modifications feature precision soldering and IPS backlit displays.",
                        buttonText = "Explore Retro Hardware",
                        buttonUrl = "#shop"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.TIMELINE,
                        title = "Our 4-Stage Cartridge & Mod Certification",
                        subtitle = "How our retro engineers restore vintage gaming hardware",
                        content = "01 Ultrasonic Board Deoxidation: We clean vintage PCB connectors using electronics-grade ultrasonic baths to eliminate 30 years of tarnish.|02 EEPROM Verification & Flashing: Cartridge memory chips undergo full bitwise verification and fresh battery SRAM soldering for 20+ year save files.|03 Custom Shell Casting & UV Print: Precision injection molded shells are finished with scratch-resistant metallic labels and holo foil seals.|04 Real Hardware Stress Test: Every unit is played continuously for 2 hours on unmodified original consoles to guarantee rock-solid stability."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.FAQ,
                        title = "Ordering & International Shipping FAQ",
                        subtitle = "Everything you need to know about Akiba 4-Bit orders",
                        content = "How does WhatsApp Checkout work?: Add any cartridge or console to your cart, proceed through the 3-step checkout, and tap Send Order. WhatsApp will open with your complete invoice and address ready to send!|Can I upload custom artwork for console printing?: Yes! In Step 3 of checkout, attach your 4-bit pixel artwork or reference photo and our Akihabara workshop will UV print it!|Are the cartridges compatible with original consoles?: Yes, 100% compatible with original hardware (Game Boy, Famicom, Super Famicom, Mega Drive) as well as modern FPGA clones!|What payment options are available?: We accept UPI (with ₹10 instant discount) and Cash on Delivery.",
                        buttonText = "Chat with Akiba Workshop",
                        buttonUrl = "https://wa.me/919876543210"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.FOOTER,
                        title = "Akiba 4-Bit ~ 秋葉原 Retro Hardware",
                        subtitle = "Radio Kaikan • Akihabara • Tokyo",
                        content = "© 2026 Akiba 4-Bit. Built with Web Builder."
                    )
                )
            }
        ),

        TemplateDefinition(
            id = "premier_4bit_pixel_commission_wizard",
            name = "Chibi Craft ~ 4-Bit Bespoke Pixel Art Wizard",
            description = "Premier multi-step bespoke 4-bit pixel art commission wizard with sprite tier selection, reference sketch photo upload, palette choosing, and instant WhatsApp booking.",
            category = "⭐ Premier & 4-Bit",
            themePreset = "retro-arcade-4bit",
            fontFamily = "'DotGothic16', sans-serif",
            badge = "4-BIT WIZARD",
            isPremier = true,
            createWebsite = {
                WebsiteEntity(
                    title = "Chibi Craft ~ 4-Bit Pixel Studio",
                    slug = "chibi-craft-commissions",
                    description = "Premier custom 4-bit pixel art commissions, VTuber sprites, and retro game animations with instant multi-step quote builder.",
                    themePreset = "retro-arcade-4bit",
                    fontFamily = "'DotGothic16', sans-serif",
                    buttonStyle = "pixel-4bit",
                    buttonRadius = "sharp",
                    customPrimaryColor = "#39FF14",
                    customBackgroundColor = "#050505",
                    customCss = """
.msw-wizard-body, .msw-review-card, .msw-modal-content {
  border: 3px solid #22C55E !important;
  box-shadow: 4px 4px 0px #000000 !important;
  border-radius: 0px !important;
}
.msw-btn-next, .msw-btn-whatsapp {
  font-family: inherit !important;
  border-radius: 0px !important;
  border: 2px solid #000000 !important;
  box-shadow: 3px 3px 0px #000000 !important;
}
"""
                )
            },
            createBlocks = { webId ->
                listOf(
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 0,
                        type = BlockType.NAVBAR,
                        title = "CHIBI CRAFT 4-BIT",
                        content = "Commission Wizard|Sprite Tiers|Gallery|Process|Contact",
                        buttonText = "Book on WhatsApp",
                        buttonUrl = "https://wa.me/919876543210"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.HERO,
                        title = "CUSTOM 4-BIT PIXEL ART & ANIME SPRITES",
                        subtitle = "Choose your sprite resolution, attach your character reference photo, and get an instant transparent quote dispatched to WhatsApp!",
                        buttonText = "Launch Commission Wizard",
                        buttonUrl = "#wizard",
                        imageUrl = "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=1200&q=80",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.MULTISTEP_WIZARD,
                        title = "4-Bit Pixel Art Commission Wizard",
                        subtitle = "Select your sprite tier, attach character photos or Pinterest sketches, and lock in your delivery slot",
                        content = "16x16 Micro Chibi Icon: ₹799: Perfect for Twitch sub badges, Discord emojis, and retro profile avatars: 👾 | 32x32 Battle Sprite & Idle Walk: ₹1,599: 4-frame animated idle walking sprite ideal for indie RPGs and VTuber stream overlays: ⚔️ | 64x64 Full Anime Hero Portrait: ₹2,999: Highly detailed high-contrast 4-bit anime hero bust with custom armor and weapons: 👑 | Epic Boss Battle Stage & Backdrop: ₹4,999: Full-scale animated boss monster sprite with authentic retro 4-bit parallax landscape: 🐉",
                        buttonText = "Send Commission Request to WhatsApp",
                        buttonUrl = "919876543210",
                        imageUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=1200&q=80",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.FEATURES,
                        title = "WHAT MAKES OUR 4-BIT CRAFT LEGENDARY",
                        subtitle = "True pixel precision drawn one dot at a time",
                        content = "Dot-By-Dot Hand Craft: No automated filters or AI pixelation; every single dot and shadow cluster is hand-placed by master pixel animators | Strict 4-Bit Color Palettes: Authentic 16-color retro constraint matching vintage PC-98, Game Boy Color, and Capcom CPS-1 arcade boards | Commercial Game License Included: Full commercial rights, royalty-free distribution, and transparent PNG sprite sheets included with all tiers | Layered Aseprite & PSD Source Files: Clean source project files with separate hair, armor, and weapon layers for easy custom animation in your engine"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.COUNTDOWN_TIMER,
                        title = "NOVEMBER COMMISSION SLOTS CLOSING",
                        subtitle = "Only 4 custom slots remaining for this month's sprint. Lock in your slot before queue closes!",
                        content = "2026-11-30T23:59:59Z",
                        buttonText = "RESERVE COMMISSION SLOT",
                        buttonUrl = "#wizard"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.FOOTER,
                        title = "Chibi Craft ~ 4-Bit Pixel Studio",
                        subtitle = "Handcrafted in Tokyo • Delivered Worldwide",
                        content = "© 2026 Chibi Craft. All Rights Reserved."
                    )
                )
            }
        ),

        TemplateDefinition(
            id = "premier_4bit_cyber_mech_lounge",
            name = "Neo-Shibuya Mecha Lounge & 4-Bit Cyber Bar",
            description = "Premier cybernetic anime lounge and VIP esports booking portal with pod reservation wizard, drink packages, photo reference upload, and WhatsApp concierge.",
            category = "⭐ Premier & 4-Bit",
            themePreset = "anime-4bit",
            fontFamily = "'Silkscreen', monospace",
            badge = "4-BIT VIP LOUNGE",
            isPremier = true,
            createWebsite = {
                WebsiteEntity(
                    title = "Neo-Shibuya Mecha Lounge",
                    slug = "neo-shibuya-cyber-lounge",
                    description = "Premier 4-bit cyber lounge and mecha esports bar in Tokyo. VIP booth booking wizard and WhatsApp concierge.",
                    themePreset = "anime-4bit",
                    fontFamily = "'Silkscreen', monospace",
                    buttonStyle = "pixel-4bit",
                    buttonRadius = "sharp",
                    customPrimaryColor = "#FF2A85",
                    customBackgroundColor = "#0F051D",
                    customCss = """
.msw-wizard-body, .msw-review-card {
  border: 3px solid #FF2A85 !important;
  box-shadow: 4px 4px 0px #000000 !important;
  border-radius: 0px !important;
}
.msw-btn-next, .msw-btn-whatsapp {
  font-family: inherit !important;
  border-radius: 0px !important;
  border: 2px solid #000000 !important;
  box-shadow: 3px 3px 0px #000000 !important;
}
"""
                )
            },
            createBlocks = { webId ->
                listOf(
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 0,
                        type = BlockType.NAVBAR,
                        title = "NEO-SHIBUYA 4-BIT",
                        content = "VIP Booking|Cyber Drinks|Arcade Arena|Reviews|Location",
                        buttonText = "Reserve VIP Pod",
                        buttonUrl = "https://wa.me/919876543210"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.HERO,
                        title = "NEO-SHIBUYA 4-BIT CYBER LOUNGE & MECHA ARENA",
                        subtitle = "Tokyo's Premier Underworld Cyberpunk Anime Bar. Experience retro arcade tournaments, glowing pixel ramune cocktails, and private VR mecha pods.",
                        buttonText = "BOOK VIP POD",
                        buttonUrl = "#wizard",
                        imageUrl = "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?w=1200&q=80",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.MULTISTEP_WIZARD,
                        title = "VIP Pod & Table Reservation Wizard",
                        subtitle = "Select your pod experience, pick your cyber snack tier, and confirm with our WhatsApp concierge",
                        content = "Cyber Cocktail Booth (2-4 Pilots): ₹1,299: Reserved neon pod with 4 signature pixel ramune cocktails & retro Japanese snack flight: 🍹 | Arcade Battle Station (Up to 6): ₹2,499: 2-hour unlimited retro cabinet battles, craft brews & loaded pixel yakitori platters: 🕹️ | VIP Mecha Commander Suite: ₹4,999: Private holographic gaming room, dedicated maid server & premium bottle service: 👑 | Tournament Entry & Pilot Pass: ₹899: Solo bracket tournament seat with commemorative 4-bit metal pilot badge: ⚡",
                        buttonText = "Confirm Reservation via WhatsApp",
                        buttonUrl = "919876543210",
                        imageUrl = "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=1200&q=80",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.FEATURES,
                        title = "SIGNATURE 4-BIT CYBER GASTRONOMY",
                        subtitle = "Infused with neon lasers and retro Japanese street food heritage",
                        content = "Plasma Ramune Highball: Japanese sparkling citrus soda with butterfly pea flower gin and edible glowing gold stars | Neon Karaage Overdrive: Ultra-crispy double-fried chicken dusted with spicy 4-bit sansho pepper and wasabi aioli | Pixel Wagyu Bento: Melt-in-mouth A5 Miyazaki beef over seasoned sushi rice served in a vintage Famicom-style bento box | Glitch Parfait Supreme: Layers of matcha cake, black sesame ice cream, and popping candy that crackles like a CRT screen"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.COUNTDOWN_TIMER,
                        title = "NEXT GLOBAL MECHA BATTLE SHOWDOWN",
                        subtitle = "Annual Neo-Tokyo Cyber Tournament with live broadcast to over 40 countries!",
                        content = "2026-12-12T20:00:00Z",
                        buttonText = "SECURE ARENA PASS",
                        buttonUrl = "#wizard"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.FOOTER,
                        title = "NEO-SHIBUYA 4-BIT CYBER LOUNGE",
                        subtitle = "Shibuya Udagawacho Underground • Tokyo",
                        content = "© 2026 Neo-Shibuya Cyber Lounge. All Systems Go."
                    )
                )
            }
        ),

        TemplateDefinition(
            id = "premier_4bit_sound_lab",
            name = "Vocal-Bit ~ ヴォーカルビット 4-Bit Soundchip Store",
            description = "Premier 4-bit chiptune sound design and physical Game Boy flash cartridge store with instant WhatsApp ordering, audio stems, cart drawer, and custom sound design options.",
            category = "⭐ Premier & 4-Bit",
            themePreset = "gameboy-4bit",
            fontFamily = "'VT323', monospace",
            badge = "4-BIT SOUND LAB",
            isPremier = true,
            createWebsite = {
                WebsiteEntity(
                    title = "Vocal-Bit ~ ヴォーカルビット",
                    slug = "vocal-bit-sound-store",
                    description = "Premier 4-bit soundchip cartridges, vocaloid stems, and retro Game Boy audio hardware with WhatsApp checkout.",
                    themePreset = "gameboy-4bit",
                    fontFamily = "'VT323', monospace",
                    buttonStyle = "pixel-4bit",
                    buttonRadius = "sharp",
                    customPrimaryColor = "#0F380F",
                    customBackgroundColor = "#8B956D",
                    customCss = """
.kw-product-card, .kw-cart-drawer, .kw-modal-content {
  border: 3px solid #0F380F !important;
  box-shadow: 4px 4px 0px #0F380F !important;
  border-radius: 0px !important;
  background: #9BBC0F !important;
  color: #0F380F !important;
}
.kw-add-btn, .kw-wa-btn {
  font-family: inherit !important;
  border-radius: 0px !important;
  background: #0F380F !important;
  color: #9BBC0F !important;
  border: 2px solid #0F380F !important;
  box-shadow: 3px 3px 0px #306230 !important;
}
"""
                )
            },
            createBlocks = { webId ->
                listOf(
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 0,
                        type = BlockType.NAVBAR,
                        title = "VOCAL-BIT ヴォーカルビット",
                        content = "Soundpacks|Hardware|ROM Cartridges|Stems|FAQ",
                        buttonText = "Order on WhatsApp",
                        buttonUrl = "https://wa.me/919876543210"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.WHATSAPP_SHOP,
                        title = "Vocal-Bit 4-Bit Soundchips & Chiptunes",
                        subtitle = "Authentic analog Game Boy sound ROMs, loop cartridges, and anime vocaloid stems",
                        content = "Nanoloop 2.8 FM Synth Cartridge: 2199: 3999: 5.0: Authentic Game Boy Advance 16-step analog FM synthesizer cartridge: https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=600&q=80: Hardware | 4-Bit Anime Battle Jingle Pack: 699: 1499: 4.9: 150+ royalty-free square wave victory fanfares and UI sound effects: https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&q=80: Audio Packs | Cyber-Chiptune Full OST Album: 999: 1999: 5.0: 18-track high-fidelity FLAC album with complete LSDJ tracker stem files: https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&q=80: Albums | Custom Game Boy Shell & Glow Buttons: 1499: 2999: 4.8: Hand-painted anime mecha DMG-01 shell with tactile clicky switches: https://images.unsplash.com/photo-1531525645387-7f14be1bdbbd?w=600&q=80: Hardware",
                        buttonText = "Send Order to WhatsApp",
                        buttonUrl = "919876543210",
                        imageUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=1200&q=80",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.ABOUT,
                        title = "Analog 4-Bit Sound Synthesizers",
                        subtitle = "Directly from the Iconic 1989 Sharp LR35902 Sound Chip",
                        content = "We sample, design, and flash raw square waves, 4-bit noise channels, and pulse-width modulators directly using vintage hardware. Used by acclaimed indie game developers and vocaloid producers worldwide.",
                        buttonText = "Listen to Stems",
                        buttonUrl = "#shop"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.TIMELINE,
                        title = "Soundpack & Hardware Crafting Pipeline",
                        subtitle = "From analog synthesizer recording to flash cartridge production",
                        content = "01 Vintage DMG-01 Direct Recording: Analog audio recorded through audiophile-grade ProSound modified line-out jacks with zero line noise.|02 Mastering for Modern Speakers: Multi-band dynamic compression tuned to sound crystal clear on modern smartphones, headphones, and TV soundbars.|03 Tracker File Packaging: Every track includes complete original LSDJ & Nanoloop project save states for your remixing.|04 Physical Cartridge Flashing: Flash memory cartridges tested across original Game Boy, Game Boy Pocket, and Game Boy Color."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.FAQ,
                        title = "Audio Licensing & Shipping FAQ",
                        subtitle = "Clear answers on commercial usage and delivery",
                        content = "Can I use these soundpacks in commercial games?: Yes! All soundpacks include 100% royalty-free commercial game and stream licenses.|How do I receive digital stems?: After submitting your order via WhatsApp, digital download codes with 24-bit FLAC stems are sent instantly in chat!|Do you ship physical cartridges internationally?: Yes! We ship physical cartridges worldwide via DHL Express with tracking provided in WhatsApp.",
                        buttonText = "Inquire on WhatsApp",
                        buttonUrl = "https://wa.me/919876543210"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.FOOTER,
                        title = "Vocal-Bit ~ ヴォーカルビット",
                        subtitle = "Tokyo Underground Sound Lab • Akihabara",
                        content = "© 2026 Vocal-Bit Laboratory. Built with Web Builder."
                    )
                )
            }
        ),
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
                        buttonUrl = "#work",
                        alignment = "left"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.GALLERY,
                        title = "Selected Works & Artifacts",
                        subtitle = "Curated brand identities, kinetic systems, and physical editions",
                        content = "Aura Sound System: Minimalist acoustic hardware brand identity: https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&q=80 | Kinetic Typography 2026: Award-winning experimental type specimen: https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=800&q=80 | Lumina Monograph: Hardcover editorial art book with Japanese binding: https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=800&q=80",
                        buttonText = "View All Disciplines",
                        buttonUrl = "#services"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.ABOUT,
                        title = "About The Practice",
                        subtitle = "10+ Years of Craft in Tokyo & New York",
                        content = "Believing that technology should feel tactile and memorable. Partnering with visionary founders, luxury brands, and cultural institutions to shape products that leave lasting impressions."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.SERVICES,
                        title = "Disciplines & Offerings",
                        subtitle = "From initial sketch to high-fidelity deployment",
                        content = "🎨 Visual Brand Identity: Logomarks, art direction, and design guidelines.|📱 Next-Gen UI/UX: Interactive web & native mobile application design.|✨ Motion & 3D Art: Kinetic typography and promotional video assets."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.CONTACT,
                        title = "Let's Build Something Exceptional",
                        subtitle = "Currently taking select commissions for Q3/Q4.",
                        content = "Send project inquiries to elena@vancestudio.design",
                        buttonText = "Send Message",
                        buttonUrl = "mailto:elena@vancestudio.design"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 6,
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
                        type = BlockType.ABOUT,
                        title = "Our Roasting Heritage & Story",
                        subtitle = "Founded in Portland in 2018",
                        content = "Velvet & Grain began as an old cargo van transformed into a mobile hand-cranked espresso bar. Today, our brick-and-mortar roastery celebrates patient craft, micro-lot coffees, and communal morning warmth.",
                        buttonText = "Explore The Menu",
                        buttonUrl = "#menu"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.FEATURES,
                        title = "The Craft Behind Every Cup",
                        subtitle = "From small farmer cooperatives straight to your ceramic mug.",
                        content = "🌱 Single-Origin Direct Trade: Transparent relationships with family farms in Oaxaca and Yirgacheffe.|🔥 Micro-Batch Roasting: Roasted in small 5kg cast iron drums to highlight floral nuances.|🥐 Fermented Sourdoughs: Naturally leavened sourdough baguettes baked fresh every morning at 5 AM."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.PRICING,
                        title = "Seasonal Menu & Fresh Offerings",
                        subtitle = "Hand-poured coffees, seasonal lattes, and hearth-baked pastries",
                        content = "Ethiopian Natural Pour-Over: 5.50: 12oz: Jasmine, bergamot & wild blueberry notes with honey sweetness | Bourbon Smoked Maple Latte: 6.25: 16oz: House-made wood-smoked maple syrup, double espresso & steamed oat milk | Cardamom & Orange Morning Bun: 4.75: Fresh: Wild-fermented laminated brioche swirled with Ceylon cinnamon and fresh orange zest",
                        buttonText = "Order Ahead",
                        buttonUrl = "#contact"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.TESTIMONIALS,
                        title = "Loved By Our Community",
                        content = "\"The best cardamom bun and cleanest Ethiopian natural pour-over in the Pacific Northwest. An absolute sanctuary on rainy mornings.\"",
                        subtitle = "Eater Magazine • 2026 Cafe of the Year",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 6,
                        type = BlockType.CONTACT,
                        title = "Visit The Roastery",
                        subtitle = "428 Pine Street, Portland, Oregon",
                        content = "Open Daily: 7:00 AM – 5:00 PM | Weekend Brunch: 8:00 AM – 3:00 PM",
                        buttonText = "Get Directions",
                        buttonUrl = "https://maps.google.com"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 7,
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
                        type = BlockType.TIMELINE,
                        title = "Featured Open-Source Projects",
                        subtitle = "Distributed primitives & performance benchmarking tools",
                        content = "RaftKV: High-performance distributed key-value store in Rust with dynamic membership consensus.|NetProbe: eBPF-powered network latency profiler for Kubernetes pods with zero kernel overhead.|LogLSM: Crash-safe append-only log storage engine capable of 1M ops/sec on NVMe drives."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.CONTACT,
                        title = "Get In Touch",
                        subtitle = "Available for advisory and technical consulting.",
                        content = "Email: kaelen@thorne.systems | PGP Key Available",
                        buttonText = "Send Email",
                        buttonUrl = "mailto:kaelen@thorne.systems"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 6,
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
                        type = BlockType.TESTIMONIALS,
                        title = "Critical Praise & Reader Reflections",
                        content = "\"The single most stimulating technical publication on my reading roster each weekend. Flawlessly argued, beautiful typography, and deeply inspiring.\"",
                        subtitle = "Dr. Aris Thorne — Fellow at Institute for Software Ethics",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.ABOUT,
                        title = "About The Publication",
                        subtitle = "Independent Editorial Collective",
                        content = "The Modern Chronicle was founded to champion slow, deliberate technical journalism and design criticism. Every piece is written by practitioners and community contributors."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 6,
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
                        buttonUrl = "#work",
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
                        type = BlockType.SERVICES,
                        title = "Selected Client Partnerships",
                        subtitle = "Landmark platforms and brand systems launched in 2025-2026",
                        content = "Raycast AI Architecture: Complete web identity and reactive tactile design system.|Solana Pay Engine: Low-friction payment checkout flow for high-volume cross-border commerce.|Arcane Health: Intelligent preventive metabolic wellness platform powered by edge ML."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.TIMELINE,
                        title = "Our Execution Framework",
                        subtitle = "From whiteboarding vision to shipping high-fidelity production systems in weeks.",
                        content = "01 Discovery & Strategy: Uncovering core differentiators, user mental models, and market positioning.|02 Creative Prototyping: Interactive tactile prototypes, motion design, and system architecture.|03 Production Engineering: Pixel-perfect frontend builds, zero-latency deployment, and automated scaling."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.STATS,
                        title = "Engineered For Measurable Impact",
                        subtitle = "Key benchmarks delivered across our recent global client launches.",
                        content = "4.8x: Average Conversion Lift | 98/100: Google Lighthouse Performance | $140M+: Client Capital Raised | 12: International Design Awards",
                        buttonText = "Read Case Studies",
                        buttonUrl = "#contact"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 6,
                        type = BlockType.TEAM,
                        title = "Creative & Engineering Leadership",
                        subtitle = "A compact multidisciplinary team of senior practitioners.",
                        content = "Maya Sterling: Creative Director: Former design lead at Pentagram & Apple Design Lab.|Leo Zhang: Head of Systems: Distributed systems architect and WebGL contributor.|Amara O'Connor: Product Strategist: Ex-Stripe lead focused on conversion & retention."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 7,
                        type = BlockType.CTA,
                        title = "Have a Landmark Project in Mind?",
                        subtitle = "We take on 4 marquee client partnerships per quarter. Let's explore your timeline.",
                        buttonText = "Schedule Discovery Call",
                        buttonUrl = "mailto:hello@nexuslabs.design"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 8,
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
                        type = BlockType.PRICING,
                        title = "Curated Living Collection",
                        subtitle = "Tactile ceramics, organic linens, and natural home fragrances",
                        content = "Earthen Matcha Bowl & Whisk: 48: Edition: Wheel-thrown stoneware with speckled reactive glaze | Stone-Washed Belgian Linen Throw: 120: Pure: Breathable heavyweight waffle weave in natural flax | Hinoki Wood Botanical Candle: 36: 8oz: Wild cypress, vetiver, and smoky cedarwood in amber glass",
                        buttonText = "Add to Bag",
                        buttonUrl = "#contact"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.ABOUT,
                        title = "The Philosophy of Slow Domesticity",
                        subtitle = "Honoring natural textures and conscious everyday rituals",
                        content = "We believe homes should be quiet sanctuaries filled only with pieces that carry soul, warmth, and enduring utility. Every object in our collection is born from intimate partnerships with multigenerational family workshops in Portugal, Kyoto, and Oaxaca.",
                        buttonText = "Explore Ethical Sourcing",
                        buttonUrl = "#features"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.FEATURES,
                        title = "Why Conscious Living Chooses AURA",
                        subtitle = "Honest materials and transparent ethical craftsmanship in every detail.",
                        content = "🌿 100% Organic & Recycled: Zero synthetic dyes, biodegradable plant-based packaging.|🤲 Artisan Handcrafted: Small studio runs crafted by master ceramists in Portugal and Japan.|🚚 Carbon-Neutral Shipping: Every order offset through verified reforestation reserves."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.STATS,
                        title = "Our Sustainability Footprint",
                        subtitle = "Measurable commitment to ethical manufacturing and fair living wages.",
                        content = "100%: Plastic-Free Packaging | 32,000+: Happy Conscious Homes | 4,200+: Trees Planted in 2026 | 0%: Animal Byproducts"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 6,
                        type = BlockType.TESTIMONIALS,
                        title = "Words From Our Community",
                        content = "\"The heavyweight waffle throw and matcha ceramic cup are sublime. They have completely elevated my morning ritual.\"",
                        subtitle = "Camille Laurent — Architectural Digest Reviewer",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 7,
                        type = BlockType.FAQ,
                        title = "Orders & Care Questions",
                        subtitle = "Helpful information regarding shipping, gift packaging, and product care.",
                        content = "How do I care for stoneware?: Hand wash recommended, microwave and dishwasher safe up to 250°C.|Where do you ship?: We ship carbon-neutral worldwide with tracking provided within 24 hours.|What is your return policy?: Enjoy 30 days of hassle-free returns on all domestic purchases."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 8,
                        type = BlockType.NEWSLETTER,
                        title = "The AURA Slow Living Journal",
                        subtitle = "Weekly reflections on architecture, intentional living, and mindful domestic spaces.",
                        buttonText = "Subscribe Free",
                        buttonUrl = "#newsletter"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 9,
                        type = BlockType.CTA,
                        title = "Enjoy 15% Off Your First Order",
                        subtitle = "Join our journal for seasonal releases, slow design essays, and exclusive archive sales.",
                        buttonText = "Claim 15% Code",
                        buttonUrl = "#contact"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 10,
                        type = BlockType.FOOTER,
                        title = "AURA Goods Co.",
                        content = "© 2026 AURA Goods Co. Crafted with care for slow living.",
                        subtitle = "Instagram • Pinterest • Fair Trade Certified"
                    )
                )
            }
        ),

        TemplateDefinition(
            id = "cyberpunk_gaming_studio",
            name = "NeonStrike Esports & Game Studio",
            description = "High-octane dark cyberpunk website with neon purple and cyan glows, game features, real-time server stats, and visual gallery.",
            category = "Cyber & Gaming",
            themePreset = "cyber-neon",
            fontFamily = "JetBrains Mono, monospace",
            badge = "CYBER NEON",
            createWebsite = {
                WebsiteEntity(
                    title = "NeonStrike Studios",
                    slug = "neonstrike-studios",
                    description = "Next-generation Unreal Engine sci-fi action games and competitive esports arena.",
                    themePreset = "cyber-neon",
                    fontFamily = "JetBrains Mono, monospace"
                )
            },
            createBlocks = { webId ->
                listOf(
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 0,
                        type = BlockType.NAVBAR,
                        title = "NEON//STRIKE",
                        content = "Games|Engine|Tournaments|Arena|Community",
                        buttonText = "Play Beta",
                        buttonUrl = "#hero"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.HERO,
                        title = "ENTER THE HYPER-SPEED SCI-FI COMBAT ARENA",
                        subtitle = "Experience zero-latency tactical warfare rendered in Unreal Engine 5 with full neural net physics and global crossplay.",
                        buttonText = "Download Early Access",
                        buttonUrl = "#features",
                        secondaryButtonText = "Watch Cinematic",
                        secondaryButtonUrl = "#gallery",
                        imageUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=1200&auto=format&fit=crop&q=80",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.STATS,
                        title = "BATTLE ARENA TELEMETRY",
                        subtitle = "Real-time performance metrics measured across our global edge cluster",
                        content = "120 FPS: Ultra Ray-Tracing | 4.8M: Global Warriors | 18: Esports Trophies | 99.99%: Battle Cluster SLA"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.FEATURES,
                        title = "NEXT-GEN GAMING ARCHITECTURE",
                        subtitle = "Engineered from the ground up for ruthless competitive balance",
                        content = "⚡ Sub-Millisecond Netcode: Custom UDP rollback netcode guarantees flawless hit-registration.|🧠 Neural AI Combatants: Reactive enemies adapt in real-time to your squad tactics and flanks.|🌌 Procedural Cyber Worlds: Dynamic ray-traced neon mega-cities with destructible terrain.|🛠️ Full Community Modding: Lua-powered SDK lets creators forge weapons, maps, and game modes."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.GALLERY,
                        title = "BATTLEFIELD VISUAL ARCHIVES",
                        subtitle = "In-game screenshots captured natively at 4K resolution",
                        content = "Neon District Night Raid: https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600&q=80 | Cyber Mech Assembly: https://images.unsplash.com/photo-1538481199705-c710c4e965fc?w=600&q=80 | Quantum Warp Drive: https://images.unsplash.com/photo-1511512578047-dfb367046420?w=600&q=80 | Synthetic Champion: https://images.unsplash.com/photo-1563089145-599997674d42?w=600&q=80"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.FAQ,
                        title = "SYSTEM SPECS & ACCESS FAQ",
                        content = "What platforms are supported?: NeonStrike is native on Windows PC, PlayStation 5, and Xbox Series X/S with full cross-progression.|Is Early Access free to play?: Yes! Jump in for free with rotation champions. Cosmetic battle passes fund future tournament prize pools.|How do I join the competitive league?: Link your Discord or Steam profile in the Arena tab to get ranked in weekly automated cups.",
                        buttonText = "Join Official Discord",
                        buttonUrl = "https://discord.com"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 6,
                        type = BlockType.FOOTER,
                        title = "NEON//STRIKE STUDIOS",
                        subtitle = "Steam • PlayStation • Xbox • Epic Games",
                        content = "© 2026 NeonStrike Studios Inc. Powered by Web Builder."
                    )
                )
            }
        ),

        TemplateDefinition(
            id = "luxury_jewelry_atelier",
            name = "Maison Aurum Haute Jewelry",
            description = "Prestigious luxury atelier with champagne gold and onyx aesthetic, fine typography, diamond collections, and private concierge appointments.",
            category = "Luxury & Fashion",
            themePreset = "luxury-gold",
            fontFamily = "Playfair Display, serif",
            badge = "HAUTE LUXE",
            createWebsite = {
                WebsiteEntity(
                    title = "Maison Aurum Fine Jewelry",
                    slug = "maison-aurum-jewelry",
                    description = "Bespoke high-jewelry, certified conflict-free diamonds, and master Italian goldsmithing.",
                    themePreset = "luxury-gold",
                    fontFamily = "Playfair Display, serif"
                )
            },
            createBlocks = { webId ->
                listOf(
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 0,
                        type = BlockType.NAVBAR,
                        title = "MAISON AURUM",
                        content = "Collections|Atelier|High Jewelry|Heritage|Concierge",
                        buttonText = "Private Viewing",
                        buttonUrl = "#contact"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.HERO,
                        title = "Pure Goldsmithing Sculpted for Eternity",
                        subtitle = "Discover bespoke high-jewelry creations hand-forged from recycled 18k solid gold and certified ethical diamond solitaires.",
                        buttonText = "Explore High Jewelry",
                        buttonUrl = "#gallery",
                        secondaryButtonText = "Book Private Concierge",
                        secondaryButtonUrl = "#contact",
                        imageUrl = "https://images.unsplash.com/photo-1515562141207-7a88fb7ce338?w=1200&auto=format&fit=crop&q=80",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.ABOUT,
                        title = "The Florence Goldsmithing Tradition",
                        subtitle = "Four Decades of Rare Gemstone Connoisseurship",
                        content = "Each Maison Aurum piece is sculpted over hundreds of hours by master lapidaries in our historic Florence studio. We personally hand-select rare emeralds from Colombia, unheated Ceylon sapphires, and D-color flawless diamonds.",
                        buttonText = "Our Ethical Guarantee",
                        buttonUrl = "#features"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.GALLERY,
                        title = "Signature High Jewelry Creations",
                        subtitle = "One-of-a-kind museum-grade heirlooms for discerning collectors",
                        content = "Solitaire Brilliant Cut Diamond: https://images.unsplash.com/photo-1605100804763-247f67b3557e?w=600&q=80 | 18K Hammered Gold Choker: https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?w=600&q=80 | Tahitian Black Pearl Drop: https://images.unsplash.com/photo-1535632066927-ab7c9ab60908?w=600&q=80 | Colombian Emerald Cluster: https://images.unsplash.com/photo-1611591475152-4735d387e949?w=600&q=80"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.TESTIMONIALS,
                        title = "Collector Accolades",
                        subtitle = "Celebrated by fine jewelry connoisseurs worldwide",
                        content = "The craftsmanship of the custom solitaire engagement ring is breathtaking. The diamond fire is incomparable.: Countess Evelyn V.: Zurich, Switzerland | Maison Aurum created our family heritage necklace with such poetic grace and precision. Truly timeless.: Marcus Sterling: London & Geneva | A rare sanctuary of pure Italian artisanal jewelry. The private consultation was exceptional.: Diane Chen: Hong Kong"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.FOOTER,
                        title = "MAISON AURUM HAUTE JOAILLERIE",
                        subtitle = "Florence • Geneva • Paris • New York",
                        content = "© 2026 Maison Aurum. All rights reserved. Created with Web Builder."
                    )
                )
            }
        ),

        TemplateDefinition(
            id = "fitness_crossfit_club",
            name = "Apex Iron Athletic Club",
            description = "High-energy athletic club and CrossFit landing page with crimson racing accents, program schedules, facility stats, and free trial pass booking.",
            category = "Fitness & Health",
            themePreset = "crimson-energy",
            fontFamily = "Inter, sans-serif",
            badge = "HIGH OCTANE",
            createWebsite = {
                WebsiteEntity(
                    title = "Apex Iron Club",
                    slug = "apex-iron-club",
                    description = "State-of-the-art strength facility, Hyrox training center, and elite conditioning.",
                    themePreset = "crimson-energy",
                    fontFamily = "Inter, sans-serif"
                )
            },
            createBlocks = { webId ->
                listOf(
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 0,
                        type = BlockType.NAVBAR,
                        title = "APEX IRON CLUB",
                        content = "Programs|Coaches|Schedule|Pricing|Trial",
                        buttonText = "Free 3-Day Pass",
                        buttonUrl = "#pricing"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.HERO,
                        title = "FORGE UNBREAKABLE STRENGTH & PEAK ENDURANCE",
                        subtitle = "Train in our 20,000 sq ft industrial athletic facility with Olympic barbells, Hyrox conditioning arenas, and science-backed coaching.",
                        buttonText = "Claim Free Trial Pass",
                        buttonUrl = "#pricing",
                        secondaryButtonText = "Explore Programs",
                        secondaryButtonUrl = "#features",
                        imageUrl = "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=1200&auto=format&fit=crop&q=80",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.STATS,
                        title = "RESULTS-DRIVEN FACILITY METRICS",
                        content = "850+: Active Athletes | 34: Elite Strength Coaches | 12: National Titles | 24/7: Keycard Access"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.FEATURES,
                        title = "ELITE TRAINING DISCIPLINES",
                        subtitle = "Built for dedicated athletes, beginners, and competitors alike",
                        content = "🏋️ Olympic Weightlifting: 18 dedicated Eleiko lifting platforms with calibrated competition plates.|🔥 Hyrox & Functional MetCon: High-intensity rowing, ski-ergs, sled pushes, and lunges for engine conditioning.|❄️ Cryotherapy & Cold Plunge: Contrast therapy recovery lounge with infrared saunas and cold immersion tubs.|🥗 Performance Fuel Bar: Fresh macro-balanced protein shakes, cold-pressed juices, and electrolyte taps."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.PRICING,
                        title = "MEMBERSHIP TIERS",
                        subtitle = "No hidden sign-up fees. Pause or cancel anytime.",
                        content = "Strength Open Gym: $49/mo: 24/7 keycard access to all free weights, machines & locker suites. | Unlimited CrossFit & MetCon: $119/mo: Full access + unlimited daily coached group training, barbell clinics & sauna. | Elite VIP Athlete: $199/mo: Unlimited classes + 2x monthly personal training, InBody scan & nutrition plan."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.CTA,
                        title = "YOUR TRANSFORMATION STARTS TODAY",
                        subtitle = "Experience the energy of Apex Iron with a zero-commitment 3-day guest trial pass.",
                        buttonText = "Get Free 3-Day Pass",
                        buttonUrl = "#pricing"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 6,
                        type = BlockType.FOOTER,
                        title = "APEX IRON ATHLETIC CLUB",
                        subtitle = "2400 Industrial Way • Open 24/7",
                        content = "© 2026 Apex Iron Club. Built with Web Builder."
                    )
                )
            }
        ),

        TemplateDefinition(
            id = "pastel_matcha_cafe",
            name = "Matcha & Mochi Sweet Parlor",
            description = "Dreamy pastel aesthetic cafe with soft strawberry and matcha tones, fluffy soufflé pancakes, photo gallery, and reservation inquiries.",
            category = "Hospitality",
            themePreset = "pastel-candy",
            fontFamily = "Inter, sans-serif",
            badge = "SWEET PASTEL",
            createWebsite = {
                WebsiteEntity(
                    title = "Matcha & Mochi Parlor",
                    slug = "matcha-and-mochi",
                    description = "Artisan Kyoto ceremonial matcha lattes, jiggly soufflé pancakes, and fresh handmade mochi.",
                    themePreset = "pastel-candy",
                    fontFamily = "Inter, sans-serif"
                )
            },
            createBlocks = { webId ->
                listOf(
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 0,
                        type = BlockType.NAVBAR,
                        title = "Matcha & Mochi 🌸",
                        content = "Menu|Soufflé Pancakes|Matcha Bar|Gallery|Visit Us",
                        buttonText = "Reserve Table",
                        buttonUrl = "#contact"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.HERO,
                        title = "Artisan Ceremonial Matcha & Cloud Soufflé Pancakes",
                        subtitle = "Indulge in farm-direct Uji green tea, ultra-fluffy Japanese soufflé stacks, and strawberry daifuku baked fresh every morning.",
                        buttonText = "Explore Sweet Menu",
                        buttonUrl = "#features",
                        secondaryButtonText = "View Gallery",
                        secondaryButtonUrl = "#gallery",
                        imageUrl = "https://images.unsplash.com/photo-1536256263959-770b48d82b0a?w=1200&auto=format&fit=crop&q=80",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.FEATURES,
                        title = "Our Handcrafted Delights",
                        subtitle = "Every treat is prepared with pure natural ingredients and gentle love",
                        content = "🍵 Ceremonial Uji Matcha: Whisked fresh to order using single-origin stoneground tea leaves from Kyoto.|🥞 Jiggly Soufflé Pancakes: Triple-stacked airy cloud pancakes topped with organic berries and honey cream.|🍓 Strawberry Daifuku: Soft hand-pounded sweet rice dough wrapped around ripe juicy strawberries and red bean.|☀️ Sunny Botanical Patio: Beautiful pastel pink and matcha green indoor garden with sunny pet-friendly outdoor patio."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.GALLERY,
                        title = "A Taste of Sweet Moments",
                        subtitle = "Follow us on Instagram for daily fresh bake reveals",
                        content = "Iced Strawberry Matcha Cloud: https://images.unsplash.com/photo-1544787219-7f47ccb76574?w=600&q=80 | Fluffy Soufflé Stack: https://images.unsplash.com/photo-1528207776546-365bb710ee93?w=600&q=80 | Handmade Daifuku Mochi: https://images.unsplash.com/photo-1563729784474-d77dbb933a9e?w=600&q=80 | Matcha Latte Art: https://images.unsplash.com/photo-1517256064527-09c73fc73e38?w=600&q=80"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.FAQ,
                        title = "Visiting Matcha & Mochi",
                        content = "Do you take reservations?: Yes! Use our contact form to reserve tables for groups of 4 or more. Walk-ins are always welcomed for our pastry counter.|Are there dairy-free milk options?: Absolutely! We serve organic oat milk, almond milk, and coconut cloud cream at no extra charge.|Can I order custom pastry gift boxes?: Yes, we prepare assorted mochi gift boxes wrapped in Japanese Furoshiki fabric with 24 hours notice.",
                        buttonText = "Order a Gift Box",
                        buttonUrl = "#contact"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.FOOTER,
                        title = "Matcha & Mochi Sweet Parlor",
                        subtitle = "Open Daily 8:00 AM - 7:00 PM • Sunny Botanical Terrace",
                        content = "© 2026 Matcha & Mochi. Built with Web Builder."
                    )
                )
            }
        ),

        TemplateDefinition(
            id = "indie_podcast_studio",
            name = "Frequency Wave Audio Lab",
            description = "Modern electric synthwave podcast website with episode tracklist, audio player aesthetic, guest highlights, and subscriber perks.",
            category = "Media & Podcast",
            themePreset = "retro-synth",
            fontFamily = "Inter, sans-serif",
            badge = "AUDIO WAVE",
            createWebsite = {
                WebsiteEntity(
                    title = "Frequency Wave Podcast",
                    slug = "frequency-wave-podcast",
                    description = "Deep conversations on artificial intelligence, cybernetics, space exploration, and the future of humanity.",
                    themePreset = "retro-synth",
                    fontFamily = "Inter, sans-serif"
                )
            },
            createBlocks = { webId ->
                listOf(
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 0,
                        type = BlockType.NAVBAR,
                        title = "FREQUENCY//WAVE",
                        content = "Episodes|Guests|Live Shows|VIP Feed|Newsletter",
                        buttonText = "Listen on Spotify",
                        buttonUrl = "https://spotify.com"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.HERO,
                        title = "UNCOMPROMISED CONVERSATIONS AT THE EDGE OF REALITY",
                        subtitle = "Join weekly deep dives with visionary engineers, quantum physicists, and digital philosophers reshaping human consciousness.",
                        buttonText = "Listen to Latest Episode",
                        buttonUrl = "#features",
                        secondaryButtonText = "Join VIP Member Feed",
                        secondaryButtonUrl = "#pricing",
                        imageUrl = "https://images.unsplash.com/photo-1590602847861-f357a9332bbc?w=1200&auto=format&fit=crop&q=80",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.STATS,
                        title = "GLOBAL PODCAST REACH",
                        content = "180+: Produced Episodes | 4.6M: Total Listens | 4.9★: Apple Podcasts | #1: Indie Science Show"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.FEATURES,
                        title = "WHAT MAKES FREQUENCY SPECIAL",
                        subtitle = "Zero corporate fluff. Just unscripted curiosity and breakthrough discoveries.",
                        content = "🎙️ 3-Hour Unfiltered Dialogues: We dive past soundbites into first-principles mechanics of mind, code, and cosmos.|🎧 Mastered in Dolby Atmos: Recorded with studio-grade Shure mics and custom analog compression for warm intimacy.|🔮 Uncut Video Streams: Watch high-definition multi-camera live video streams on YouTube and Patreon.|💬 Private Backchannel Community: Join 12,000+ engineers, creators, and thinkers in our private Discord lounge."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.TESTIMONIALS,
                        title = "Listener Reflections",
                        subtitle = "What our global community has to say",
                        content = "Frequency Wave is the only show that genuinely challenges how I perceive synthetic intelligence and bioengineering.: Dr. Sarah Vance: Neurotech Researcher | The audio production and question depth make every 3-hour episode feel like 20 minutes. Mandatory weekly listening.: Ethan Cole: Roboticist & Founder | Found my co-founder inside the Frequency Discord lounge after episode #142!: Priya Sharma: Space Startup CEO"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.CTA,
                        title = "NEVER MISS A GROUNDBREAKING EPISODE",
                        subtitle = "Subscribe on Spotify, Apple Podcasts, YouTube, or RSS.",
                        buttonText = "Subscribe on Spotify",
                        buttonUrl = "https://spotify.com"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 6,
                        type = BlockType.FOOTER,
                        title = "FREQUENCY WAVE PODCAST & LABS",
                        subtitle = "Spotify • Apple Podcasts • YouTube • Overcast",
                        content = "© 2026 Frequency Wave Media. Created with Web Builder."
                    )
                )
            }
        ),

        TemplateDefinition(
            id = "artisan_coffee_roastery",
            name = "Komorebi Artisan Specialty Coffee",
            description = "Third-wave micro-roastery showcase with single-origin tasting flights, bean subscription tiers, origin stories, and direct checkout.",
            category = "Hospitality",
            themePreset = "warm-editorial",
            fontFamily = "Playfair Display, serif",
            badge = "SPECIALTY",
            createWebsite = {
                WebsiteEntity(
                    title = "Komorebi Roasters",
                    slug = "komorebi-coffee",
                    description = "Single-origin micro-lot specialty coffees roasted weekly in Kyoto & dispatched worldwide.",
                    themePreset = "warm-editorial",
                    fontFamily = "Playfair Display, serif"
                )
            },
            createBlocks = { webId ->
                listOf(
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 0,
                        type = BlockType.NAVBAR,
                        title = "KOMOREBI COFFEE",
                        content = "Origins|Harvest Menu|Subscription|Brew Guide|Locations",
                        buttonText = "Order Beans",
                        buttonUrl = "#pricing"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.HERO,
                        title = "ETHICALLY SOURCED. LIGHT ROASTED. EXTRAORDINARY CLARITY.",
                        subtitle = "Direct-trade micro-lot beans harvested from altitude farms in Ethiopia, Colombia, and Costa Rica. Roasted every Tuesday on cast-iron vintage drum roasters.",
                        buttonText = "Explore Fresh Harvest",
                        buttonUrl = "#pricing",
                        secondaryButtonText = "Our Kyoto Brew Bar",
                        secondaryButtonUrl = "#about",
                        imageUrl = "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=1200&auto=format&fit=crop&q=80",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.STATS,
                        title = "OUR CRAFT IN NUMBERS",
                        content = "2,150m: Peak Elevation | 89.5+: SCA Cup Score | 100%: Direct-Trade | 48hr: Dispatch Post-Roast"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.FEATURES,
                        title = "CURRENT SEASON MICRO-LOTS",
                        subtitle = "Tasting notes crafted in sensory evaluation cuppings",
                        content = "Yirgacheffe Floral Mist (Ethiopia): Heirloom varietal washed process with radiant notes of bergamot, honeysuckle, and candied Meyer lemon.|Gesha Cerro Azul (Colombia): Anaerobic natural fermentation highlighting wild strawberry, jasmine tea, and dark cocoa nibs.|Tarrazú Honey Reserve (Costa Rica): Yellow honey processed Caturra with golden honey body, crisp Fuji apple, and toasted macadamia.|Mount Kenya Peaberry (Kenya): Bright sparkling phosphoric acidity bursting with blackcurrant, ruby grapefruit, and brown sugar cane."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.PRICING,
                        title = "FRESH ROAST SUBSCRIPTIONS",
                        subtitle = "Shipped freshly roasted to your doorstep in nitrogen-sealed recyclable bags with free grind sizing",
                        content = "Tasting Explorer: ₹899: 2x 250g monthly bags • Rotating single-origin discovery • Free pour-over brew guide • Flexible pause anytime: #subscribe | Connoisseur Flight: ₹1,649: 4x 250g bags • Reserve micro-lots & Gesha rarities • Priority early access to limited drops • Free express courier: #subscribe | Espresso Bar Bulk: ₹3,200: 2kg whole bean bag • Roasted specifically for 9-bar high extraction • Dial-in recipe card • Wholesale price per cup: #subscribe",
                        buttonText = "Join Coffee Club",
                        buttonUrl = "#subscribe"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.TESTIMONIALS,
                        title = "Brew Enthusiasts Love Komorebi",
                        subtitle = "Trusted by home baristas and world-champion competitors",
                        content = "The Yirgacheffe is genuinely one of the cleanest, most fragrant natural coffees I've tasted in 10 years.: Marcus Vance: Q-Grader & Barista Champion | Their subscription turned my morning routine into a luxury pour-over sanctuary.: Elena Rostova: Architect & Coffee Collector | Incredible clarity, zero bitterness, and arrives 2 days after roasting. Perfection.: David Kim: Creative Director"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 6,
                        type = BlockType.CONTACT,
                        title = "VISIT OUR KYOTO BREW BAR & ROASTERY",
                        subtitle = "Open Tuesday through Sunday • Cupping sessions every Saturday morning at 10 AM",
                        content = "📍 42-1 Higashiyama-ku, Kyoto 605-0862, Japan\n📞 +81 75 551 2890\n✉️ hello@komorebicoffee.jp\n🕒 Tue - Sun: 08:00 - 18:00 (Closed Mondays)",
                        buttonText = "Send an Inquiry",
                        buttonUrl = "mailto:hello@komorebicoffee.jp"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 7,
                        type = BlockType.FOOTER,
                        title = "KOMOREBI ARTISAN COFFEE ROASTERS",
                        subtitle = "Kyoto • Tokyo • Worldwide Direct Trade Dispatch",
                        content = "© 2026 Komorebi Specialty Coffee. Handcrafted with Web Builder."
                    )
                )
            }
        ),

        TemplateDefinition(
            id = "saas_ai_developer_hub",
            name = "Nexus AI Neural API Cloud",
            description = "High-tech developer platform for foundation LLM models, edge inferences, real-time embeddings, and serverless compute with code snippets.",
            category = "Startup & Tech",
            themePreset = "cyber-neon",
            fontFamily = "Fira Code, monospace",
            badge = "DEVELOPER",
            createWebsite = {
                WebsiteEntity(
                    title = "Nexus AI Cloud",
                    slug = "nexus-ai-cloud",
                    description = "Sub-10ms neural inferences and vector indexing for next-generation intelligent applications.",
                    themePreset = "cyber-neon",
                    fontFamily = "Fira Code, monospace"
                )
            },
            createBlocks = { webId ->
                listOf(
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 0,
                        type = BlockType.NAVBAR,
                        title = "NEXUS.AI",
                        content = "Models|Benchmarks|Docs|Playground|Pricing",
                        buttonText = "Get API Key",
                        buttonUrl = "#pricing"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.HERO,
                        title = "ENTERPRISE NEURAL INFERENCE AT 120 TOKENS/SEC",
                        subtitle = "Deploy custom weights, streaming embeddings, and function-calling agents with single-digit latency across 32 global edge regions. Zero cold starts.",
                        buttonText = "Start Free • 100k Tokens",
                        buttonUrl = "#pricing",
                        secondaryButtonText = "Interactive API Playground",
                        secondaryButtonUrl = "#features",
                        imageUrl = "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=1200&auto=format&fit=crop&q=80",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.STATS,
                        title = "INFRASTRUCTURE PERFORMANCE",
                        content = "9.2ms: Median TTFT | 32 Regions: Global Edge Mesh | 99.99%: Uptime SLA | 4.8B: Tokens Ingested / Day"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.FEATURES,
                        title = "ENGINEERED FOR PRODUCTION WORKLOADS",
                        subtitle = "Everything software teams need to scale autonomous intelligence",
                        content = "⚡ Flash-Attention Kernel Accelerators: Custom Triton kernels delivering 4x throughput on NVIDIA H100 clusters with dynamic batching.|🔒 Confidential Enclave Execution: Run fine-tuned proprietary enterprise models inside hardware-isolated AMD SEV-SNP enclaves.|🧩 Native JSON Schema Validation: Guaranteed schema extraction with zero hallucinations and microsecond output parsing.|🌐 Multi-Modal Token Pipeline: Ingest images, high-res PDF blueprints, and audio streams in a unified unified latent embedding."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.PRICING,
                        title = "SIMPLE, PREDICTABLE DEVELOPER PRICING",
                        subtitle = "Scale from indie prototypes to billions of monthly tokens with zero hidden fees",
                        content = "Hacker Sandbox: $0 / mo: 100,000 free tokens / month • Community Discord support • 32 concurrent requests • Standard latency queue: #get-api-key | Pro Engineering: $49 / mo: 5,000,000 tokens included • Sub-10ms priority inference • 256 concurrent requests • Custom fine-tuning weights • Dedicated slack channel: #get-api-key | Enterprise Dedicated: $499 / mo: Dedicated H100 reservation • Custom VPC peering • BAA & HIPAA compliance • 99.99% uptime SLA guarantee • 24/7 on-call engineers: #contact",
                        buttonText = "Create Developer Account",
                        buttonUrl = "#pricing"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.FAQ,
                        title = "Developer FAQ",
                        content = "How compatible is the Nexus API with OpenAI SDKs?: 100% drop-in compatible. Simply update baseURL to https://api.nexusai.cloud/v1 and pass your Nexus API key.|Where are your inference servers located?: We operate GPU clusters in US-East, US-West, Frankfurt, Tokyo, Singapore, and Mumbai.|Do you train on customer data?: Never. We enforce zero data retention (ZDR) by default across all tiers with cryptographic verification.",
                        buttonText = "Read Documentation",
                        buttonUrl = "https://docs.nexusai.cloud"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 6,
                        type = BlockType.FOOTER,
                        title = "NEXUS AI SYSTEMS INC.",
                        subtitle = "San Francisco • Zurich • Tokyo",
                        content = "© 2026 Nexus AI Systems. Built with Web Builder."
                    )
                )
            }
        ),

        TemplateDefinition(
            id = "tokyo_luxury_wellness_spa",
            name = "Zenith Onsen & Luxury Sanctuary",
            description = "Serene Japanese onsen and holistic mindfulness retreat with thermal mineral bath packages, massage rituals, and appointment bookings.",
            category = "Luxury & Fashion",
            themePreset = "minimal-light",
            fontFamily = "Cinzel, serif",
            badge = "LUXURY SPA",
            createWebsite = {
                WebsiteEntity(
                    title = "Zenith Sanctuary",
                    slug = "zenith-onsen-sanctuary",
                    description = "Architectural hot-spring sanctuary offering cedar sauna rituals and organic botanical skincare.",
                    themePreset = "minimal-light",
                    fontFamily = "Cinzel, serif"
                )
            },
            createBlocks = { webId ->
                listOf(
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 0,
                        type = BlockType.NAVBAR,
                        title = "ZENITH SANCTUARY",
                        content = "Bath Rituals|Holistic Treatments|Villas|Dining|Reservations",
                        buttonText = "Reserve Experience",
                        buttonUrl = "#pricing"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.HERO,
                        title = "IMMERSIVE HOT SPRINGS & RESTORATIVE SILENCE",
                        subtitle = "Nestled among ancient bamboo groves, Zenith combines geothermal mineral waters with traditional Japanese stone therapy and bespoke aromatherapy.",
                        buttonText = "Explore Bath Rituals",
                        buttonUrl = "#pricing",
                        secondaryButtonText = "Sanctuary Suites",
                        secondaryButtonUrl = "#about",
                        imageUrl = "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=1200&auto=format&fit=crop&q=80",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.ABOUT,
                        title = "The Philosophy of Yugen",
                        subtitle = "Profound grace and quiet harmony",
                        content = "At Zenith, water is reverence. Sourced from 800 meters underground, our sulfur and silica-rich geothermal pools restore cellular vitality and calm the nervous system in an atmosphere of architectural quietude.",
                        buttonText = "Discover Our Mineral Springs",
                        buttonUrl = "#features"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.FEATURES,
                        title = "SANCTUARY SIGNATURE EXPERIENCES",
                        subtitle = "Curated multi-sensory treatments crafted for restorative renewal",
                        content = "♨️ Geothermal Mineral Immersion: Five cascading thermal pools infused with volcanic minerals, white clay, and hinoki cypress oil.|🌿 Bamboo Charcoal Body Exfoliation: Gentle full-body buffing ritual with crushed volcanic pumice, green tea, and camellia oil.|🍵 Ceremonial Matcha Facial: Organic ceremonial grade Uji matcha antioxidant masque with lymphatic jade stone drainage.|🔥 Hinoki Cedar Dry Sauna: Pure Japanese aromatic hinoki wood sauna with cold plunge and Himalayan salt breathing chambers."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.PRICING,
                        title = "DAY SANCTUARY PASSES & PRIVATE RETREATS",
                        subtitle = "All bookings include private locker suites, artisan herbal tea ceremony, and organic amenities",
                        content = "Thermal Mineral Day Pass: $180: Unlimited access to 5 geothermal pools • Hinoki cedar sauna & steam room • Artisan matcha service • Organic robe & slipper set: #book | The Kuroshio Rejuvenation: $360: Full thermal immersion pass • 90-minute volcanic stone massage • Uji matcha botanical facial • Bento lunch at Tea Pavilion: #book | Private Villa Twilight Sanctuary: $750: Exclusive evening access to private hillside onsen • 120-minute couple's treatment • Kaiseki dinner for two • Champagne service: #book",
                        buttonText = "Book Your Reservation",
                        buttonUrl = "#pricing"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.TESTIMONIALS,
                        title = "Guest Journal",
                        subtitle = "Voices of restorative peace",
                        content = "The cedar scent, the steaming hot springs under the stars, and the attentive silence. The greatest sanctuary in the world.: Julian Thorne: Creative Architect | Stepping into Zenith felt like time stopped. My tension completely evaporated within minutes.: Mei Ling Chen: Fashion Director | The private hillside villa and thermal bath package was an unforgettable anniversary celebration.: Alexander & Sarah Bell: London"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 6,
                        type = BlockType.FOOTER,
                        title = "ZENITH ONSEN & SANCTUARY",
                        subtitle = "Hakone Geothermal Valley, Kanagawa, Japan",
                        content = "© 2026 Zenith Sanctuary Group. Crafted with Web Builder."
                    )
                )
            }
        ),

        TemplateDefinition(
            id = "cyber_neon_esports_studio",
            name = "Vortex Apex Gaming & Esports Clan",
            description = "High-energy esports team portal with tournament schedules, roster player cards, live Twitch streaming integration, and pro merch gear.",
            category = "Cyber & Gaming",
            themePreset = "cyber-neon",
            fontFamily = "Orbitron, sans-serif",
            badge = "ESPORTS PRO",
            createWebsite = {
                WebsiteEntity(
                    title = "Vortex Clan",
                    slug = "vortex-esports-clan",
                    description = "Championship esports organization competing in Tier-1 Valorant, Apex Legends, and Counter-Strike.",
                    themePreset = "cyber-neon",
                    fontFamily = "Orbitron, sans-serif"
                )
            },
            createBlocks = { webId ->
                listOf(
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 0,
                        type = BlockType.NAVBAR,
                        title = "VORTEX//APEX",
                        content = "Roster|Tournaments|Highlights|Merch|Discord",
                        buttonText = "Join Discord",
                        buttonUrl = "https://discord.gg"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.HERO,
                        title = "DEFY GRAVITY. DOMINATE THE ARENA.",
                        subtitle = "Home of the 3x World Champions in Tactical Shooters. Powered by hyper-reflex talent and cutting-edge mechanical execution.",
                        buttonText = "Watch Live Stream",
                        buttonUrl = "https://twitch.tv",
                        secondaryButtonText = "View Active Roster",
                        secondaryButtonUrl = "#features",
                        imageUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=1200&auto=format&fit=crop&q=80",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.STATS,
                        title = "CHAMPIONSHIP ACHIEVEMENTS",
                        content = "3x: World Titles | $2.4M: Tournament Winnings | 1.8M: Community Followers | 48: Global Trophy Victories"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.FEATURES,
                        title = "ACTIVE ROSTER & PRO ATHLETES",
                        subtitle = "Meet the squad leading our 2026 World Championship campaign",
                        content = "🎯 'CYPHER' Jaxson Lee (IGL / Entry): Tactical shotcaller with 42% headshot accuracy and master game awareness in high-pressure rounds.|⚡ 'VALKYRIE' Mia Tanaka (Duelist): World-record fastest ace in championship history with pinpoint sniper reflexes.|🛡️ 'AEGIS' Roman Petrov (Support / Anchor): Clutch master known for unbreakable defensive holds and tactical utility usage.|🔮 'NEO' Alex Chen (Flex / Controller): Unpredictable smoke execution specialist with 1.45 tournament K/D ratio."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.CTA,
                        title = "GEAR UP WITH OFFICIAL PRO MERCH",
                        subtitle = "Custom jerseys, ultra-glide magnetic mechanical keyboards, and precision speed mousepads designed by our champions.",
                        buttonText = "Shop Pro Collection",
                        buttonUrl = "https://vortexgear.gg"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.FOOTER,
                        title = "VORTEX APEX ESPORTS ENTERTAINMENT",
                        subtitle = "Los Angeles • Seoul • Berlin",
                        content = "© 2026 Vortex Clan Gaming. Built with Web Builder."
                    )
                )
            }
        ),

        TemplateDefinition(
            id = "dental_aesthetic_clinic",
            name = "Apex Smile Studio & Cosmetic Dentistry",
            description = "High-end dental practice and cosmetic smile makeover clinic with service packages, dentist credentials, patient before/after reviews, and direct WhatsApp appointment booking.",
            category = "Medical & Dental",
            themePreset = "minimal-light",
            fontFamily = "Plus Jakarta Sans, sans-serif",
            badge = "CLINIC PRO",
            createWebsite = {
                WebsiteEntity(
                    title = "Apex Smile Studio",
                    slug = "apex-smile-studio",
                    description = "Pain-free digital dentistry, porcelain veneers, laser teeth whitening, and Invisalign orthodontic care.",
                    themePreset = "minimal-light",
                    fontFamily = "Plus Jakarta Sans, sans-serif"
                )
            },
            createBlocks = { webId ->
                listOf(
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 0,
                        type = BlockType.NAVBAR,
                        title = "APEX SMILE",
                        content = "Treatments|Technology|Doctors|Pricing|Book Visit",
                        buttonText = "Book Consultation",
                        buttonUrl = "https://wa.me/919876543210"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.HERO,
                        title = "PRECISION DIGITAL DENTISTRY & CONFIDENT SMILES",
                        subtitle = "Experience stress-free, pain-free dental care with 3D intraoral optical scanning, painless laser whitening, and ultra-thin custom porcelain veneers in a soothing spa environment.",
                        buttonText = "Book Smile Consultation",
                        buttonUrl = "#pricing",
                        secondaryButtonText = "View Before & After",
                        secondaryButtonUrl = "#about",
                        imageUrl = "https://images.unsplash.com/photo-1629909613654-28e377c37b09?w=1200&auto=format&fit=crop&q=80",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.STATS,
                        title = "CLINICAL EXCELLENCE IN NUMBERS",
                        content = "12,000+: Radiant Smiles Created | 15+ Yrs: Clinical Expertise | 99.4%: Pain-Free Rating | 0%: Interest EMI Available"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.FEATURES,
                        title = "ADVANCED COSMETIC & RESTORATIVE CARE",
                        subtitle = "State-of-the-art procedures performed with zero needle pain and microscope precision",
                        content = "✨ Ultra-Thin Porcelain Veneers: Minimal-prep handcrafted ceramic veneers custom shaded to complement your facial harmony.|😁 Invisalign Clear Orthodontics: Invisible 3D-mapped orthodontic aligners for teeth straightening without metal brackets or food restrictions.|💎 ZOOM® Laser Power Whitening: Safe, enamel-safe LED photo-activation lightening teeth up to 8 shades in a single 45-minute visit.|🔬 3D Guided Dental Implants: Precision digital CT-scan computer navigation for lifelong replacement of missing teeth."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.PRICING,
                        title = "TRANSPARENT CLINIC PACKAGES",
                        subtitle = "All appointments include 3D digital intraoral scan, panoramic X-ray, and personalized treatment preview",
                        content = "Preventative & Spa Polish: ₹1,800: Comprehensive ultrasonic scaling • Air-flow stain polish • Digital dental X-ray • Fluoride protective seal: #book | Laser Whitening Glow: ₹6,500: Full 3-cycle ZOOM® in-chair whitening • Enamel remineralizing treatment • Home touch-up maintenance kit: #book | Complete Smile Makeover: ₹24,000+: 3D virtual smile simulator • Custom ceramic mock-up • Master ceramist fitting • Comprehensive 5-year guarantee: #book",
                        buttonText = "Schedule Appointment",
                        buttonUrl = "#contact"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.TESTIMONIALS,
                        title = "Patient Stories",
                        subtitle = "Real results from patients who overcame dental anxiety",
                        content = "I used to be terrified of dental visits. Dr. Ananya and the team made my veneer treatment completely painless and relaxing!: Priya Mehta: Fashion Designer | The 3D scan and Invisalign plan was so easy to follow. My teeth are perfectly straight after just 7 months!: Rohan Kapoor: Tech Entrepreneur | Hands down the most modern and hygienic clinic I've ever experienced. Highly recommended!: Dr. Vikram Sengupta: Orthopedic Surgeon"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 6,
                        type = BlockType.CONTACT,
                        title = "VISIT APEX SMILE STUDIO",
                        subtitle = "Valet parking available • Open 7 days a week with emergency on-call doctors",
                        content = "📍 Suite 402, Platinum Medical Enclave, Indiranagar, Bengaluru\n📞 +91 80 4122 8899\n💬 WhatsApp: +91 98765 43210\n🕒 Mon - Sat: 09:00 - 20:00 | Sun: 10:00 - 16:00",
                        buttonText = "Request Dental Appointment",
                        buttonUrl = "mailto:appointments@apexsmilestudio.com"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 7,
                        type = BlockType.FOOTER,
                        title = "APEX SMILE DENTAL & AESTHETIC STUDIO",
                        subtitle = "Bengaluru • Mumbai • Accredited Dental Council",
                        content = "© 2026 Apex Smile Studio. Created with Web Builder."
                    )
                )
            }
        ),

        TemplateDefinition(
            id = "real_estate_luxury_villas",
            name = "Azure Horizon Mediterranean Villas",
            description = "Ultra-luxury waterfront real estate portal with architectural property showcases, floorplan specs, video tour CTA, and private broker concierge.",
            category = "Real Estate & Villas",
            themePreset = "minimal-light",
            fontFamily = "Cinzel, serif",
            badge = "LUXURY REALTY",
            createWebsite = {
                WebsiteEntity(
                    title = "Azure Horizon Realty",
                    slug = "azure-horizon-villas",
                    description = "Private waterfront villas and cliffside estates along the Mediterranean coastline.",
                    themePreset = "minimal-light",
                    fontFamily = "Cinzel, serif"
                )
            },
            createBlocks = { webId ->
                listOf(
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 0,
                        type = BlockType.NAVBAR,
                        title = "AZURE HORIZON",
                        content = "Estates|Architecture|Amenities|Private Viewings|Concierge",
                        buttonText = "Private Viewing",
                        buttonUrl = "#contact"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.HERO,
                        title = "CLIFFSIDE WATERFRONT SANCTUARIES IN MALLORCA & CAPRI",
                        subtitle = "Exceptional private residences featuring cantilevered infinity pools, private yacht moorings, and uninterrupted 270-degree Mediterranean horizon vistas.",
                        buttonText = "Explore Available Estates",
                        buttonUrl = "#pricing",
                        secondaryButtonText = "Watch Cinematic Film",
                        secondaryButtonUrl = "#about",
                        imageUrl = "https://images.unsplash.com/photo-1613977257363-707ba9348227?w=1200&auto=format&fit=crop&q=80",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.STATS,
                        title = "PORTFOLIO EXCLUSIVITY",
                        content = "€140M+: Managed Real Estate Assets | 18: Ultra-Private Estates | 100%: Direct Waterfront Access | 24/7: Dedicated Estate Concierge"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.FEATURES,
                        title = "SIGNATURE RESIDENTIAL AMENITIES",
                        subtitle = "Architectural elegance meets world-class resort living",
                        content = "🌊 Heated Sea-Salt Infinity Edge Pools: Seamlessly blending into the Mediterranean horizon with underwater sound systems and travertine stone decks.|🛥️ Private Deep-Water Yacht Slip: Direct ocean access mooring capable of accommodating vessels up to 45 meters with fueling facilities.|🍷 Climate-Controlled Wine Cellar: Subterranean limestone cellars storing 2,500+ bottles with integrated tasting lounge.|🌿 Olive Grove Gardens & Helipad: Private landscaped Mediterranean botanical gardens with registered private helicopter landing pad."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.PRICING,
                        title = "FEATURED AVAILABLE RESIDENCES",
                        subtitle = "Discreetly listed private estates ready for immediate handover",
                        content = "Villa Solaria (Mallorca): €6,850,000: 6 Bedrooms • 8 Bathrooms • 780 m² living space • Heated infinity pool • Private beach stairs: #inquire | Villa Terrazza (Capri): €11,200,000: 7 Bedrooms • Cliffside terrace overlooking Faraglioni • Yacht mooring • Guest villa & spa: #inquire | The Horizon Penthouse (Monaco): €18,500,000: 5 Suites • Rooftop garden • 360° harbor panorama • 6 private garage bays • Full concierge: #inquire",
                        buttonText = "Schedule Private Inspection",
                        buttonUrl = "#contact"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.CONTACT,
                        title = "CONTACT OUR PRIVATE CLIENT BROKERAGE",
                        subtitle = "Strict confidentiality guaranteed for all prospective acquirers and private family offices",
                        content = "📍 Boulevard de la Croisette, Cannes / Port d'Andratx, Mallorca\n📞 +33 4 93 39 00 12\n✉️ privateclient@azurehorizonvillas.com\n🔒 Discrete viewings scheduled via private jet charter or helicopter transfer.",
                        buttonText = "Inquire Discreetly",
                        buttonUrl = "mailto:privateclient@azurehorizonvillas.com"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 6,
                        type = BlockType.FOOTER,
                        title = "AZURE HORIZON PRIVATE ESTATES",
                        subtitle = "Monaco • Cannes • Mallorca • Capri",
                        content = "© 2026 Azure Horizon International Realty. Built with Web Builder."
                    )
                )
            }
        ),

        TemplateDefinition(
            id = "electronic_music_festival",
            name = "Solstice Electronic Arts & Music Festival",
            description = "High-energy festival and music event landing page with lineup headliners, stage stages, multi-tier pass tickets, and countdown experience.",
            category = "Music & Festival",
            themePreset = "cyber-neon",
            fontFamily = "Orbitron, sans-serif",
            badge = "FESTIVAL PASS",
            createWebsite = {
                WebsiteEntity(
                    title = "Solstice Festival",
                    slug = "solstice-music-festival",
                    description = "Three days of transcendent electronic music, immersive lasers, and generative digital art under the desert stars.",
                    themePreset = "cyber-neon",
                    fontFamily = "Orbitron, sans-serif"
                )
            },
            createBlocks = { webId ->
                listOf(
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 0,
                        type = BlockType.NAVBAR,
                        title = "SOLSTICE 2026",
                        content = "Lineup|Stages|Experience|Camping|Tickets",
                        buttonText = "Get Passes",
                        buttonUrl = "#pricing"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.HERO,
                        title = "WHERE FREQUENCY TRANSCENDS REALITY",
                        subtitle = "August 14-16, 2026 • Mojave Desert Basin. 4 immersive spatial audio stages, 60+ world-class electronic artists, and cutting-edge holographic installations.",
                        buttonText = "Secure Early Bird Passes",
                        buttonUrl = "#pricing",
                        secondaryButtonText = "Watch 2025 Aftermovie",
                        secondaryButtonUrl = "#about",
                        imageUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=1200&auto=format&fit=crop&q=80",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.STATS,
                        title = "THE FESTIVAL EXPERIENCE",
                        content = "3 Days & Nights: Non-Stop Sound | 4 Stages: Spatial Audio Engineered | 60+ Artists: Global Live Electronic Lineup | 25,000: Kindred Spirits"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.FEATURES,
                        title = "2026 HEADLINE ARTISTS & SOUNDSCAPES",
                        subtitle = "Curated across melodic techno, ambient modular, and futuristic bass",
                        content = "🌌 TALE OF SOLARIS (Live 3-Hour Sunset Odyssey): Groundbreaking cinematic modular synths paired with synchronized laser pyramids.|⚡ KINETIC MIND (B2B Neural Void): Raw analog modular techno driving hypnotic polyrhythmic energy until sunrise.|🎆 AURA SPHERE (Audio-Visual Holo Dome): 360-degree immersive dome with real-time reactive AI visual projections.|🍃 RESONANCE GROVE: 24-hour chillout sanctuary with crystal sound baths, ambient drone, and organic elixir bar."
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.PRICING,
                        title = "FESTIVAL PASS TIERS & CAMPING ACCESS",
                        subtitle = "All passes include reusable RFID wristband and free hydration stations across all stages",
                        content = "General Admission (3-Day): $249: Full 3-day access to all 4 stages • Free filtered water refills • Food village & art walk access • Standard shuttle service: #buy | VIP Mirage Experience: $489: Expedited festival entry • VIP viewing platforms with private bar • Air-conditioned luxury restrooms • Commemorative merch gift: #buy | Desert Oasis Glamping (2 Persons): $1,199: 2x VIP Passes • Furnished yurt with memory foam beds • AC unit & power outlets • Dedicated concierge & lounge: #buy",
                        buttonText = "Purchase Festival Pass",
                        buttonUrl = "#pricing"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.FAQ,
                        title = "Festival Information & Safety",
                        content = "Are payment plans available for passes?: Yes, you can lock in your ticket with a $50 deposit and 4 equal monthly installments.|What are the camping check-in hours?: Campgrounds open Thursday August 13 at 12:00 PM and close Monday August 17 at 12:00 PM.|Is there a shuttle from major airports?: Yes! Direct festival shuttles run continuously from Las Vegas (LAS) and Palm Springs (PSP).",
                        buttonText = "View Festival Survival Guide",
                        buttonUrl = "https://solsticefestival.com/info"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 6,
                        type = BlockType.FOOTER,
                        title = "SOLSTICE MUSIC & DIGITAL ARTS FESTIVAL",
                        subtitle = "Mojave Basin • Earth • August 2026",
                        content = "© 2026 Solstice Arts Collective. Built with Web Builder."
                    )
                )
            }
        ),

        // ==========================================
        // 🎌 ANIME & 4-BIT RETRO TEMPLATES
        // ==========================================
        TemplateDefinition(
            id = "anime_4bit_vtuber",
            name = "Aoi Ch. 葵 4-Bit VTuber Studio",
            description = "Multi-page 4-bit retro anime streamer hub with live raid stream countdown, chiptune highlights, fan club membership tiers, stream schedule, and retro merch shop.",
            category = "🎌 Anime & 4-Bit",
            themePreset = "anime-4bit",
            fontFamily = "'Press Start 2P', monospace",
            badge = "4-BIT VTUBER",
            isPremier = true,
            createWebsite = {
                WebsiteEntity(
                    title = "Aoi Ch. 葵 4-Bit Hub",
                    slug = "aoi-channel-4bit",
                    description = "Official 4-bit retro anime streamer and VTuber hub featuring chiptunes, stream schedule, retro merch, and VIP fan club.",
                    themePreset = "anime-4bit",
                    fontFamily = "'Press Start 2P', monospace",
                    buttonStyle = "pixel-4bit",
                    buttonRadius = "sharp",
                    customPrimaryColor = "#FF2A85",
                    customBackgroundColor = "#0F051D",
                    pagesJson = """[{"slug":"index","title":"Home"},{"slug":"schedule","title":"Schedule"},{"slug":"merch","title":"Merch Shop"},{"slug":"fanclub","title":"Fan Club"}]""",
                    customCss = """
.hero-title {
  text-shadow: 3px 3px 0px #000000, 0 0 25px rgba(255, 42, 133, 0.7);
  letter-spacing: 0.05em;
}
.hero-badge {
  background: #FF2A85 !important;
  color: #FFFFFF !important;
  border: 2px solid #000000 !important;
  box-shadow: 3px 3px 0px #000000 !important;
}
.feature-card, .pricing-card, .countdown-card, .faq-item {
  border: 3px solid #000000 !important;
  box-shadow: 5px 5px 0px #000000 !important;
  background: #1B0A33 !important;
}
"""
                )
            },
            createBlocks = { webId ->
                listOf(
                    // Home Page (index)
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 0,
                        type = BlockType.NAVBAR,
                        pageSlug = "index",
                        title = "★ AOI CH. 葵 ★",
                        content = "Home|Schedule|Merch|Fan Club",
                        buttonText = "▶ WATCH LIVE",
                        buttonUrl = "https://twitch.tv"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.HERO,
                        pageSlug = "index",
                        title = "AOI CHANNEL 4-BIT ★ 葵",
                        subtitle = "Chiptunes, retro speedruns, and late-night anime chaos broadcasted in glorious 4-bit pixel art!",
                        buttonText = "▶ ENTER STREAM",
                        buttonUrl = "https://twitch.tv",
                        imageUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=1000&q=80",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.COUNTDOWN_TIMER,
                        pageSlug = "index",
                        title = "NEXT 4-BIT RAID STREAM",
                        subtitle = "Live Japanese JRPG 24-Hour Marathon Speedrun with Chat Room Chaos!",
                        content = "2026-11-28T20:00:00Z",
                        buttonText = "SET ALARM (TWITCH)",
                        buttonUrl = "https://twitch.tv"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.FEATURES,
                        pageSlug = "index",
                        title = "STREAM HIGHLIGHTS",
                        subtitle = "Choose your cartridge and join the fun",
                        content = "Chiptune Concerts: 4-bit live synthesizer jams, vocaloid covers, and Friday DJ sets | Speedrun Trials: Beating vintage 80s & 90s JRPGs without taking any damage | Fan Art Showcase: Reviewing weekly community chibi artwork, memes, and pixel badges | Sub Battle Brawlers: Playing retro arcade beat-em-ups multiplayer with guild subscribers"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.PRICING,
                        pageSlug = "index",
                        title = "JOIN THE 4-BIT GUILD",
                        subtitle = "Unlock pixel badges, sub discord channels, and stream sound effects",
                        content = "PIXEL NOVICE: 4.99: Custom Chat Emotes|Sub-Only Discord Access|Weekly Chibi Wallpaper: Join Novice: false | 8-BIT WARRIOR: 9.99: Everything in Novice|Monthly Cartridge Sticker Pack|Name on Stream End-Credits|Access to Friday Gaming Jams: Level Up: true | 16-BIT CHAMPION: 24.99: All Previous Perks|Physical Acrylic Chibi Standee|Custom Chiptune Song Request|VIP Discord Voice Lounge: Ultimate Champion: false"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.FAQ,
                        pageSlug = "index",
                        title = "STREAM FAQ",
                        subtitle = "Answers for new chat adventurers",
                        content = "What is 4-bit aesthetic?: It represents the golden era of pixel art: high contrast neon colors, crunchy audio waveforms, and scanline nostalgia! | How can I submit fan art?: Share your drawings on Twitter/X with #AoiPixelArt or post them directly in our Discord art gallery! | When are the streaming hours?: Wednesdays, Fridays, and Sundays at 8:00 PM JST (Tokyo Time)!"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 6,
                        type = BlockType.NEWSLETTER,
                        pageSlug = "index",
                        title = "RECEIVE 4-BIT TRANSMISSIONS",
                        subtitle = "Get weekly streaming schedules, secret chibi wallpaper drops, and tournament notices straight to your inbox!",
                        buttonText = "SUBSCRIBE TO TRANSMISSION"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 7,
                        type = BlockType.FOOTER,
                        pageSlug = "index",
                        title = "AOI CH. 葵 4-BIT HUB",
                        subtitle = "Broadcasted from Tokyo with 4-Bit Love",
                        content = "© 2026 Aoi Channel Project. All Sprites & Chiptunes Crafted with Authentic 4-Bit Flavor."
                    ),

                    // Schedule Page
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 10,
                        type = BlockType.HERO,
                        pageSlug = "schedule",
                        title = "BROADCAST SCHEDULE",
                        subtitle = "Never miss a live raid, karaoke jam, or JRPG marathon. All times shown in JST.",
                        buttonText = "ADD TO GOOGLE CALENDAR",
                        buttonUrl = "https://calendar.google.com",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 11,
                        type = BlockType.FEATURES,
                        pageSlug = "schedule",
                        title = "THIS WEEK'S CARTRIDGES",
                        subtitle = "Tune in live on Twitch & YouTube",
                        content = "WEDNESDAY 20:00 JST: Retro Chrono Trigger Speedrun Part 3 with chat-controlled randomizers | FRIDAY 21:00 JST: 4-Bit Chiptune Live Concert featuring Gameboy original LSDJ compositions | SATURDAY 19:00 JST: Community Pixel Art Review & Viewer Minecraft Pixel City Build | SUNDAY 18:00 JST: Mega Man X Zero-Death Challenge with sub-chat punishments"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 12,
                        type = BlockType.CONTACT,
                        pageSlug = "schedule",
                        title = "SUGGEST A GAME OR CHALLENGE",
                        subtitle = "Have a classic 4-bit or 8-bit game you want Aoi to play? Submit your challenge!",
                        buttonText = "SUBMIT GAME REQUEST"
                    ),

                    // Merch Page
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 20,
                        type = BlockType.HERO,
                        pageSlug = "merch",
                        title = "4-BIT RETRO MERCH VAULT",
                        subtitle = "Official collectible acrylic stands, pixel keychains, holographic stickers, and limited cassette tapes.",
                        buttonText = "EXPLORE VAULT",
                        buttonUrl = "#merch-items",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 21,
                        type = BlockType.PRICING,
                        pageSlug = "merch",
                        title = "OFFICIAL GOODS COLLECTION",
                        subtitle = "Worldwide shipping available from Tokyo warehouse",
                        content = "PIXEL KEYCHAIN SET: 12.99: 4 Acrylic Double-Sided Chibi Charms|Collector's 4-Bit Backing Card|Metal Clasp: Order Keychains: false | AOI CHIBI STANDEE: 22.99: 15cm High-Gloss Acrylic Standee|Interchangeable Pixel Baseplate|Holographic Finish: Order Standee: true | CHIPTUNE OST CASSETTE: 29.99: Limited Run Neon Pink Cassette Tape|14 Original Chiptune Tracks|Fold-Out Pixel Poster + FLAC Download: Order Cassette: false"
                    ),

                    // Fan Club Page
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 30,
                        type = BlockType.HERO,
                        pageSlug = "fanclub",
                        title = "AOI VIP FAN GUILD",
                        subtitle = "Step into the secret 4-bit lounge. Get exclusive monthly voice packs, sub games, and priority convention meetups.",
                        buttonText = "JOIN GUILD",
                        buttonUrl = "#tiers",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 31,
                        type = BlockType.PRICING,
                        pageSlug = "fanclub",
                        title = "SELECT GUILD RANK",
                        subtitle = "Cancel or upgrade anytime with instant Discord role synchronization",
                        content = "BRONZE GUILD: 4.99: Secret Discord Lounge|Monthly Chibi Wallpaper Pack|Special Chat Role: Enlist: false | SILVER ACE: 14.99: All Bronze Perks|Monthly ASMR Voice Pack|Direct Mail Postcard from Tokyo|Quarterly Digital Artbook: Enlist: true | GOLD MASTER: 34.99: All Previous Perks|Monthly Signed Physical Polaroids|Live Group Discord Voice Chat|Exclusive 4-Bit Cartridge Enamel Pin: Enlist: false"
                    )
                )
            }
        ),

        TemplateDefinition(
            id = "anime_4bit_rpg",
            name = "Chibi Quest ~ ちびクエスト 4-Bit RPG",
            description = "Retro Japanese 4-bit turn-based anime tactical RPG studio with class picking, world map carousel, boss battle raid countdown, cartridge pre-order tiers, and lore FAQ.",
            category = "🎌 Anime & 4-Bit",
            themePreset = "anime-4bit",
            fontFamily = "'DotGothic16', sans-serif",
            badge = "PIXEL RPG",
            createWebsite = {
                WebsiteEntity(
                    title = "Chibi Quest ~ ちびクエスト",
                    slug = "chibi-quest-rpg",
                    description = "Authentic 4-bit anime turn-based tactical RPG with heroic chibi classes, elemental magic combos, and retro boss raids.",
                    themePreset = "anime-4bit",
                    fontFamily = "'DotGothic16', sans-serif",
                    buttonStyle = "pixel-4bit",
                    buttonRadius = "sharp",
                    customPrimaryColor = "#FBBF24",
                    customBackgroundColor = "#0C0617",
                    customCss = """
.hero-title {
  text-shadow: 2px 2px 0px #000000, 0 0 15px rgba(251, 191, 36, 0.6);
  letter-spacing: 0.08em;
}
.feature-card, .pricing-card, .countdown-card, .faq-item {
  border: 3px solid #000000 !important;
  box-shadow: 4px 4px 0px #000000 !important;
}
"""
                )
            },
            createBlocks = { webId ->
                listOf(
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 0,
                        type = BlockType.NAVBAR,
                        title = "CHIBI QUEST ちびクエスト",
                        content = "Heroes|Worlds|Boss Raid|Cartridges|FAQ|Pre-Order",
                        buttonText = "⚔ PLAY DEMO",
                        buttonUrl = "#cartridges"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.HERO,
                        title = "CHIBI QUEST ~ 運命の冒険",
                        subtitle = "An authentic 4-bit turn-based anime tactical RPG. Command your chibi heroes, invoke elemental starburst magic, and defeat the Pixel Overlord!",
                        buttonText = "PRE-ORDER CARTRIDGE",
                        buttonUrl = "#cartridges",
                        imageUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?w=1000&q=80",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.FEATURES,
                        title = "CHOOSE YOUR HERO CLASS",
                        subtitle = "Combine elemental synergies for devastating 4-bit combo finishers",
                        content = "Mage Yumiko (詠美子): Master of celestial astral magic, meteor showers, and party MP regeneration | Paladin Kenji (健次): Immovable shield bearer with legendary pixel counter-attacks and holy barriers | Rogue Sakura (さくら): Dual-dagger shadow assassin boasting 100% critical strike and poison darts | Cleric Ren (蓮): Divine restoration priest healing party HP and removing status debuffs"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.IMAGE_CAROUSEL,
                        title = "RETRO WORLD PREVIEW",
                        subtitle = "Explore hand-drawn pixel dungeons, bustling medieval towns, and floating celestial temples",
                        content = "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=1200&q=80|https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=1200&q=80|https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=1200&q=80"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.COUNTDOWN_TIMER,
                        title = "GLOBAL BOSS RAID EVENT",
                        subtitle = "Worldwide synchronous multiplayer raid against the 4-Bit Chaos Dragon!",
                        content = "2026-11-20T18:00:00Z",
                        buttonText = "JOIN RAID GUILD",
                        buttonUrl = "#contact"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.PRICING,
                        title = "CARTRIDGE & GAME EDITIONS",
                        subtitle = "Releasing on Steam, Nintendo Switch, and Limited Physical ROM Cartridge",
                        content = "STANDARD DIGITAL: 19.99: Full Digital Game (Steam/Switch)|4-Bit Digital Instruction Manual|Day-One Chibi Hero Skin Pack: Pre-Order: false | COLLECTOR'S ROM: 39.99: Everything in Standard|42-Track Chiptune OST (FLAC)|Enamel Pin Set of All 4 Heroes|Exclusive In-Game Pet Familiar: Get Collector's: true | LEGENDARY BOX: 79.99: Hand-Numbered Custom Cartridge Replica|Hardcover 120-Page Pixel Artbook|Cloth Overworld Map|Signed Certificate from Devs: Legendary Tier: false"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 6,
                        type = BlockType.FAQ,
                        title = "GAMEPLAY & COMPATIBILITY",
                        subtitle = "Answers to adventurer inquiries",
                        content = "Is the combat turn-based?: Yes! Pure turn-based tactical RPG with active timed button presses for bonus critical damage! | Does it work on Steam Deck and handhelds?: Fully verified with native 60FPS, custom pixel scalers, and full gamepad controls! | Are multiple endings included?: Yes, 4 distinct story endings depending on your guild choices and companion bonds!"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 7,
                        type = BlockType.CONTACT,
                        title = "JOIN THE ADVENTURER'S GUILD",
                        subtitle = "Apply for closed alpha playtests, report bugs, or submit custom side-quest concepts!",
                        buttonText = "SEND APPLICATION"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 8,
                        type = BlockType.FOOTER,
                        title = "CHIBI QUEST ちびクエスト",
                        subtitle = "Developed by Pixel Hearts Game Studio • Tokyo",
                        content = "© 2026 Pixel Hearts Game Studio. All sprites hand-drawn in 4-bit palette."
                    )
                )
            }
        ),

        TemplateDefinition(
            id = "anime_4bit_maid_cafe",
            name = "Cafe NekoByte ~ 猫バイト 4-Bit Cafe",
            description = "Sweet Akihabara 4-bit retro anime maid cafe with pixel-art omurice, ramune chiptune floats, maid stage show countdown, and reservation booking.",
            category = "🎌 Anime & 4-Bit",
            themePreset = "pastel-candy",
            fontFamily = "'Silkscreen', cursive",
            badge = "ANIME CAFE",
            createWebsite = {
                WebsiteEntity(
                    title = "Cafe NekoByte 猫バイト",
                    slug = "cafe-nekobyte",
                    description = "Akihabara 4-bit retro anime maid cafe featuring pixel-art omurice, ramune chiptune floats, and maid stage shows.",
                    themePreset = "pastel-candy",
                    fontFamily = "'Silkscreen', cursive",
                    buttonStyle = "pixel-4bit",
                    buttonRadius = "sharp",
                    customPrimaryColor = "#EC4899",
                    customBackgroundColor = "#FFF0F5",
                    customCss = """
.hero-title {
  color: #EC4899 !important;
  text-shadow: 2px 2px 0px #000000;
}
.feature-card, .pricing-card, .countdown-card {
  border: 3px solid #000000 !important;
  box-shadow: 4px 4px 0px #000000 !important;
}
"""
                )
            },
            createBlocks = { webId ->
                listOf(
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 0,
                        type = BlockType.NAVBAR,
                        title = "Cafe NekoByte 猫バイト",
                        content = "Menu|Maids|Stage Show|Packages|Reserve",
                        buttonText = "♥ BOOK TABLE",
                        buttonUrl = "#reserve"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.HERO,
                        title = "WELCOME HOME, MASTER! ご主人様",
                        subtitle = "Akihabara's first 4-Bit retro anime maid cafe! Enjoy pixel-art omurice drawings, chiptune karaoke performances, and sparkling ramune floats served with 100% moe love!",
                        buttonText = "RESERVE YOUR SEAT",
                        buttonUrl = "#reserve",
                        imageUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=1000&q=80",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.FEATURES,
                        title = "LEGENDARY 4-BIT CAFE MENU",
                        subtitle = "Freshly prepared and cast with magical anime spells ~ Moe Moe Kyun!",
                        content = "Pixel Omurice: Fluffy Japanese rice omelette decorated with custom ketchup pixel art of your favorite anime character | Neko Parfait Royale: 4-layer matcha and strawberry parfait with cat-ear wafers and candy star sprinkles | Chiptune Soda Float: Sparkling blue ramune soda with rich vanilla ice cream and popping candy sparks | Bento of Champions: Hand-rolled sushi, tamagoyaki, and karaage served in a collectible retro bento box"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.COUNTDOWN_TIMER,
                        title = "NEXT MAID IDOL LIVE SHOW",
                        subtitle = "Catch Maid Chocola and Maid Vanilla singing 80s anime opening anthems live!",
                        content = "2026-11-21T19:00:00Z",
                        buttonText = "RESERVE FRONT-ROW SEAT",
                        buttonUrl = "#reserve"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.TESTIMONIALS,
                        title = "MASTER & PRINCESS REVIEWS",
                        content = "\"The maids painted an adorable pixel Totoro on my omurice and sang a retro game opening theme! The warmest, most nostalgic cafe experience in Akihabara.\"",
                        subtitle = "Kenji & Sarah • Visiting from London",
                        buttonText = "Read More Reviews",
                        buttonUrl = "#reserve"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.PRICING,
                        title = "CAFE EXPERIENCE PACKAGES",
                        subtitle = "Includes 60-minute table seating, food, drinks, and maid stage entertainment",
                        content = "STANDARD TEA SET: 15.99: 1 Signature Drink|Souvenir 4-Bit Coaster|Table Magic Spell Performance: Book Standard: false | SWEET MOE COMBO: 29.99: Custom Pixel Art Omurice|Signature Drink|Photo (Cheki) with Your Maid|Dessert Parfait: Book Combo: true | VIP MASTER EXPERIENCE: 59.99: Unlimited Soft Drinks|Front-Row Stage Show Seating|Personal Song Performance|Signed Souvenir Cheki Photo & Headband: Book VIP: false"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 6,
                        type = BlockType.CONTACT,
                        title = "TABLE RESERVATION",
                        subtitle = "Reserve your table in advance to avoid long lines during weekend anime events!",
                        buttonText = "CONFIRM RESERVATION"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 7,
                        type = BlockType.FOOTER,
                        title = "Cafe NekoByte 猫バイト",
                        subtitle = "Akihabara • Soto-Kanda 3-Chome • Tokyo",
                        content = "© 2026 Cafe NekoByte. Built with 4-Bit Anime Love."
                    )
                )
            }
        ),

        TemplateDefinition(
            id = "anime_4bit_mecha",
            name = "NEO-GENESIS ~ ネオジェネシス 2026",
            description = "Futuristic 4-bit cybernetic anime mecha championship expo with unit specifications, pilot profiles, tournament countdown, and pass packages.",
            category = "🎌 Anime & 4-Bit",
            themePreset = "cyber-neon",
            fontFamily = "'Press Start 2P', monospace",
            badge = "4-BIT MECHA",
            createWebsite = {
                WebsiteEntity(
                    title = "NEO-GENESIS ネオジェネシス",
                    slug = "neo-genesis-mecha",
                    description = "Cybernetic 4-bit anime mecha championship. Pilot armored Valkyrie units and compete in the Neo-Tokyo Arena.",
                    themePreset = "cyber-neon",
                    fontFamily = "'Press Start 2P', monospace",
                    buttonStyle = "pixel-4bit",
                    buttonRadius = "sharp",
                    customPrimaryColor = "#A855F7",
                    customBackgroundColor = "#0A051B",
                    customCss = """
.hero-title {
  text-shadow: 3px 3px 0px #000000, 0 0 20px rgba(168, 85, 247, 0.7);
  letter-spacing: 0.06em;
}
.feature-card, .pricing-card, .countdown-card {
  border: 3px solid #000000 !important;
  box-shadow: 5px 5px 0px #000000 !important;
}
"""
                )
            },
            createBlocks = { webId ->
                listOf(
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 0,
                        type = BlockType.NAVBAR,
                        title = "NEO-GENESIS ネオジェネシス",
                        content = "Frames|Pilots|Tournament|Passes|Rules",
                        buttonText = "⚡ PILOT LOGIN",
                        buttonUrl = "#passes"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.HERO,
                        title = "NEO-GENESIS 2026",
                        subtitle = "The Ultimate 4-Bit Cybernetic Anime Mecha Championship. Sync your neuro-link, engage plasma thrusters, and command legendary steel Titans!",
                        buttonText = "SECURE PILOT PASS",
                        buttonUrl = "#passes",
                        imageUrl = "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?w=1000&q=80",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.FEATURES,
                        title = "MECHA ARSENAL SPECIFICATIONS",
                        subtitle = "Tier-IV 4-Bit Combat Frames Built for High-G Atmosphere Warfare",
                        content = "Unit-01: Crimson Valkyrie: High-mobility aerial mecha equipped with dual plasma beam blades and warp dash | Unit-02: Titan Golem: Heavy armor bipedal fortress with a 300mm magnetic railgun and kinetic deflector shield | Unit-03: Shadow Shinobi: Stealth reconnaissance cyber-frame with optical cloaking and electromagnetic kunai | Unit-04: Celestial Archon: Long-range singularity energy artillery platform powered by an artificial micro-pulsar"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.COUNTDOWN_TIMER,
                        title = "WORLD CHAMPIONSHIP FINALS",
                        subtitle = "Live broadcast from Neo-Tokyo Dome Hall 4 to over 50 countries worldwide!",
                        content = "2026-12-05T17:00:00Z",
                        buttonText = "STREAM TRANSMISSION",
                        buttonUrl = "#passes"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.PRICING,
                        title = "CHAMPIONSHIP ARENA PASSES",
                        subtitle = "Experience the hydraulic roar and laser battles from front-row stadium tiers",
                        content = "SPECTATOR TICKET: 29.99: General Arena Access|Commemorative 4-Bit Floor Badge|Access to Pilot Paddock: Buy Pass: false | PILOT BADGE: 79.99: All Spectator Perks|Reserved VIP Ringside Seating|1-Hour VR Mecha Simulator Flight|Official Bomber Jacket: Become Pilot: true | SYNDICATE COMMANDER: 179.99: All-Access Weekend Pass|Backstage Meet & Greet with Mecha Designers|Numbered Metal ID Card|Limited Edition 4-Bit Diecast Model: Commander Tier: false"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.FAQ,
                        title = "TOURNAMENT REGULATIONS",
                        subtitle = "Guidelines for spectators and competitors",
                        content = "Can international pilots enter?: Yes! Open online qualifiers run through November across all regions | What is the age requirement?: Pilots must be 16 or older for simulator cockpits; all ages welcome in spectator zones | Is photography permitted?: Non-flash photography of static mechas is encouraged; arena filming rules apply during live matches!"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 6,
                        type = BlockType.FOOTER,
                        title = "NEO-GENESIS ネオジェネシス",
                        subtitle = "Neo-Tokyo Defense Syndicate • Sector 7",
                        content = "© 2026 Neo-Genesis Syndicate. All Systems Operational."
                    )
                )
            }
        ),

        TemplateDefinition(
            id = "anime_4bit_chiptune_band",
            name = "Pixel Pulse ~ ピクセルパルス Synth Band",
            description = "Japanese 4-bit anime chiptune and retro synth-pop sound collective with tour schedule, cassette and vinyl pre-orders, and LSDJ tracker stems.",
            category = "🎌 Anime & 4-Bit",
            themePreset = "retro-arcade-4bit",
            fontFamily = "'VT323', monospace",
            badge = "CHIPTUNE BAND",
            createWebsite = {
                WebsiteEntity(
                    title = "Pixel Pulse ~ ピクセルパルス",
                    slug = "pixel-pulse-band",
                    description = "Underground Japanese 4-bit anime synth-pop and chiptune sound collective. Tour dates, album cassette pre-orders, and tracker stems.",
                    themePreset = "retro-arcade-4bit",
                    fontFamily = "'VT323', monospace",
                    buttonStyle = "pixel-4bit",
                    buttonRadius = "sharp",
                    customPrimaryColor = "#39FF14",
                    customBackgroundColor = "#050505",
                    customCss = """
.hero-title {
  color: #39FF14 !important;
  text-shadow: 3px 3px 0px #000000, 0 0 20px rgba(57, 255, 20, 0.7);
  letter-spacing: 0.1em;
}
.feature-card, .pricing-card, .countdown-card {
  border: 3px solid #22C55E !important;
  box-shadow: 4px 4px 0px #000000 !important;
  background: #111111 !important;
}
"""
                )
            },
            createBlocks = { webId ->
                listOf(
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 0,
                        type = BlockType.NAVBAR,
                        title = "PIXEL PULSE ピクセルパルス",
                        content = "Albums|Tour|Physical|Stems|Newsletter",
                        buttonText = "♫ LISTEN NOW",
                        buttonUrl = "#albums"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 1,
                        type = BlockType.HERO,
                        title = "PIXEL PULSE ~ ピクセルパルス",
                        subtitle = "Tokyo's Underground 4-Bit Anime Chiptune & Synth-Pop Collective. Pumping raw Game Boy sound chips and FM synthesis through modern overdrive.",
                        buttonText = "LISTEN TO ALBUM",
                        buttonUrl = "#albums",
                        imageUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=1000&q=80",
                        alignment = "center"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 2,
                        type = BlockType.FEATURES,
                        title = "DISCOGRAPHY & SOUNDTRACKS",
                        subtitle = "Handcrafted on 1989 Nintendo Game Boy & FM Yamaha synth chips",
                        content = "Album: 4-Bit Heartbeat: 12 tracks of high-octane anime opening anthems recorded on original Game Boy hardware | Album: Shibuya Neon Cyber: Melodic vaporwave and dream-pop with Japanese vocaloid guest features | Album: Boss Battle Overdrive: Hard-hitting chiptune breakcore for late-night speedruns and boss fights | Album: Starlight Melancholy: Ambient 8-bit lofi piano loops and analog synthesizer soundscapes"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 3,
                        type = BlockType.COUNTDOWN_TIMER,
                        title = "LIVE AT CLUB CYBER AKIBA",
                        subtitle = "Live audiovisual laser & chiptune synthesizer performance with live Twitch stream!",
                        content = "2026-11-14T21:00:00Z",
                        buttonText = "GET TICKETS",
                        buttonUrl = "#tour"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 4,
                        type = BlockType.PRICING,
                        title = "PHYSICAL ALBUMS & MERCH",
                        subtitle = "Limited cassette tapes, vinyl records, and open-source LSDJ tracker project files",
                        content = "DIGITAL FLAC BUNDLE: 12.00: 44kHz 24-Bit FLAC + MP3|32-Page Digital Artbook|Custom Pixel Wallpapers: Download: false | NEON CASSETTE TAPE: 24.00: Limited Neon Green Cassette Tape|High-Res Download Code|Fold-Out Pixel Poster: Order Tape: true | VINYL & TRACKER BOX: 65.00: 180g Glow-in-the-Dark Vinyl|Complete LSDJ ROM Tracker Project Files|Embroidered Band Patch: Order Box: false"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 5,
                        type = BlockType.NEWSLETTER,
                        title = "JOIN THE 4-BIT UNDERGROUND",
                        subtitle = "Receive exclusive unreleased chiptune demo tracks, concert pre-sale codes, and stem downloads directly in your inbox!",
                        buttonText = "JOIN THE MAILING LIST"
                    ),
                    WebBlockEntity(
                        websiteId = webId,
                        orderIndex = 6,
                        type = BlockType.FOOTER,
                        title = "PIXEL PULSE ピクセルパルス",
                        subtitle = "Independent Record Label • Akihabara & Shibuya",
                        content = "© 2026 Pixel Pulse Records. Recorded on 1989 Hardware."
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
