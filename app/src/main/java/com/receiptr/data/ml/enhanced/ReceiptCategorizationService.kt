package com.receiptr.data.ml.enhanced

import com.receiptr.data.ml.ReceiptData
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Advanced receipt categorization service with intelligent classification
 * Uses merchant patterns, item keywords, and ML techniques for accurate categorization
 */
@Singleton
class ReceiptCategorizationService @Inject constructor() {
    
    /**
     * Categorizes receipt using multiple signals: merchant name, items, and context
     */
    fun categorizeReceipt(receiptData: ReceiptData): ReceiptCategory {
        val merchantName = receiptData.merchantName?.lowercase() ?: ""
        val items = receiptData.items.map { it.name.lowercase() }
        val allText = receiptData.rawText.lowercase()
        
        // Calculate confidence scores for each category
        val categoryScores = ReceiptCategory.values().associateWith { category ->
            calculateCategoryScore(merchantName, items, allText, category)
        }
        
        // Return category with highest confidence score
        return categoryScores.maxByOrNull { it.value }?.key ?: ReceiptCategory.MISCELLANEOUS
    }
    
    /**
     * Calculates confidence score for a specific category
     */
    private fun calculateCategoryScore(
        merchantName: String,
        items: List<String>,
        allText: String,
        category: ReceiptCategory
    ): Float {
        var score = 0.0f
        
        // Merchant-based scoring (highest weight)
        score += getMerchantScore(merchantName, category) * 0.5f
        
        // Item-based scoring
        score += getItemScore(items, category) * 0.3f
        
        // Context-based scoring
        score += getContextScore(allText, category) * 0.2f
        
        return score.coerceIn(0.0f, 1.0f)
    }
    
    /**
     * Scores based on merchant name patterns
     */
    private fun getMerchantScore(merchantName: String, category: ReceiptCategory): Float {
        val merchantPatterns = when (category) {
            ReceiptCategory.GROCERIES -> listOf(
                "walmart", "target", "kroger", "safeway", "whole foods", "trader joe",
                "costco", "sam's club", "publix", "wegmans", "food lion", "giant",
                "supermarket", "grocery", "market", "food", "fresh", "organic"
            )
            ReceiptCategory.DINING -> listOf(
                "mcdonald", "burger king", "subway", "starbucks", "dunkin", "kfc",
                "pizza hut", "domino", "taco bell", "chipotle", "panera", "chick-fil-a",
                "restaurant", "cafe", "bistro", "diner", "bar", "grill", "kitchen",
                "food truck", "bakery", "coffee", "pizza", "sushi", "mexican", "chinese"
            )
            ReceiptCategory.TRANSPORTATION -> listOf(
                "shell", "exxon", "bp", "chevron", "mobil", "citgo", "sunoco",
                "uber", "lyft", "taxi", "bus", "metro", "train", "airline",
                "gas station", "fuel", "parking", "toll", "transit"
            )
            ReceiptCategory.ELECTRONICS -> listOf(
                "best buy", "apple", "samsung", "microsoft", "amazon", "newegg",
                "fry's", "micro center", "radioshack", "gamestop", "electronic",
                "computer", "phone", "tablet", "laptop", "tv", "camera", "headphones"
            )
            ReceiptCategory.CLOTHING -> listOf(
                "nike", "adidas", "gap", "h&m", "zara", "forever 21", "old navy",
                "macy's", "nordstrom", "jcpenney", "kohl's", "clothing", "fashion",
                "shoes", "apparel", "dress", "shirt", "pants", "jacket", "accessories"
            )
            ReceiptCategory.HEALTHCARE -> listOf(
                "cvs", "walgreens", "rite aid", "pharmacy", "hospital", "clinic",
                "doctor", "dental", "medical", "health", "prescription", "medicine",
                "urgent care", "optometry", "physical therapy"
            )
            ReceiptCategory.ENTERTAINMENT -> listOf(
                "amc", "regal", "cinemark", "netflix", "spotify", "steam", "xbox",
                "playstation", "nintendo", "movie", "theater", "cinema", "concert",
                "game", "entertainment", "music", "streaming", "subscription"
            )
            ReceiptCategory.HOME_GARDEN -> listOf(
                "home depot", "lowe's", "ikea", "bed bath", "williams sonoma",
                "wayfair", "home goods", "furniture", "garden", "hardware",
                "appliance", "decor", "kitchen", "bathroom", "bedroom", "living room"
            )
            ReceiptCategory.AUTOMOTIVE -> listOf(
                "autozone", "advance auto", "napa", "jiffy lube", "valvoline",
                "car wash", "mechanic", "auto", "vehicle", "car", "truck", "motorcycle",
                "oil change", "tire", "battery", "brake", "repair", "maintenance"
            )
            ReceiptCategory.BUSINESS -> listOf(
                "office depot", "staples", "fedex", "ups", "post office", "bank",
                "office", "business", "professional", "supplies", "shipping",
                "printing", "conference", "meeting", "workspace"
            )
            ReceiptCategory.TRAVEL -> listOf(
                "hotel", "motel", "airbnb", "booking", "expedia", "airline",
                "travel", "vacation", "trip", "flight", "accommodation", "resort",
                "car rental", "hertz", "enterprise", "budget", "avis"
            )
            ReceiptCategory.EDUCATION -> listOf(
                "university", "college", "school", "bookstore", "library",
                "education", "tuition", "textbook", "course", "training",
                "certification", "workshop", "seminar", "academic"
            )
            ReceiptCategory.UTILITIES -> listOf(
                "electric", "gas", "water", "internet", "phone", "cable",
                "utility", "bill", "service", "provider", "telecom", "energy"
            )
            ReceiptCategory.MISCELLANEOUS -> listOf()
        }
        
        return merchantPatterns.count { pattern ->
            merchantName.contains(pattern, ignoreCase = true)
        }.toFloat() / merchantPatterns.size.coerceAtLeast(1)
    }
    
    /**
     * Scores based on item names and descriptions
     */
    private fun getItemScore(items: List<String>, category: ReceiptCategory): Float {
        val itemKeywords = when (category) {
            ReceiptCategory.GROCERIES -> listOf(
                "milk", "bread", "eggs", "cheese", "meat", "chicken", "beef",
                "vegetables", "fruits", "cereal", "pasta", "rice", "flour",
                "sugar", "salt", "oil", "butter", "yogurt", "juice", "water",
                "snacks", "cookies", "chips", "candy", "frozen", "canned"
            )
            ReceiptCategory.DINING -> listOf(
                "burger", "pizza", "sandwich", "salad", "soup", "coffee", "tea",
                "soda", "beer", "wine", "appetizer", "entree", "dessert",
                "breakfast", "lunch", "dinner", "meal", "combo", "fries"
            )
            ReceiptCategory.TRANSPORTATION -> listOf(
                "gasoline", "diesel", "fuel", "oil", "car wash", "parking",
                "toll", "fare", "ticket", "ride", "trip", "mileage"
            )
            ReceiptCategory.ELECTRONICS -> listOf(
                "iphone", "android", "laptop", "desktop", "tablet", "tv",
                "monitor", "keyboard", "mouse", "headphones", "speakers",
                "camera", "battery", "charger", "cable", "software", "app"
            )
            ReceiptCategory.CLOTHING -> listOf(
                "shirt", "pants", "dress", "shoes", "jacket", "coat", "hat",
                "socks", "underwear", "belt", "bag", "purse", "jewelry",
                "watch", "sunglasses", "scarf", "gloves", "shorts", "skirt"
            )
            ReceiptCategory.HEALTHCARE -> listOf(
                "prescription", "medicine", "vitamins", "supplements", "bandages",
                "first aid", "thermometer", "blood pressure", "glucose",
                "consultation", "examination", "treatment", "therapy"
            )
            ReceiptCategory.ENTERTAINMENT -> listOf(
                "movie", "ticket", "popcorn", "game", "subscription", "streaming",
                "music", "concert", "show", "event", "book", "magazine"
            )
            ReceiptCategory.HOME_GARDEN -> listOf(
                "furniture", "table", "chair", "bed", "sofa", "lamp", "mirror",
                "curtains", "carpet", "paint", "tools", "hammer", "screwdriver",
                "plants", "seeds", "fertilizer", "pot", "garden", "lawn"
            )
            ReceiptCategory.AUTOMOTIVE -> listOf(
                "oil change", "tire", "battery", "brake", "filter", "spark plug",
                "coolant", "transmission", "engine", "repair", "maintenance",
                "car wash", "wax", "polish", "air freshener"
            )
            ReceiptCategory.BUSINESS -> listOf(
                "paper", "pen", "pencil", "notebook", "folder", "binder",
                "printer", "ink", "toner", "envelope", "stamp", "shipping",
                "office", "supplies", "meeting", "conference"
            )
            ReceiptCategory.TRAVEL -> listOf(
                "hotel", "room", "flight", "luggage", "suitcase", "travel",
                "vacation", "trip", "tour", "guide", "map", "souvenir"
            )
            ReceiptCategory.EDUCATION -> listOf(
                "textbook", "notebook", "pen", "pencil", "calculator", "backpack",
                "tuition", "course", "class", "workshop", "seminar", "training"
            )
            ReceiptCategory.UTILITIES -> listOf(
                "electricity", "gas", "water", "internet", "phone", "cable",
                "service", "bill", "monthly", "usage", "connection"
            )
            ReceiptCategory.MISCELLANEOUS -> listOf()
        }
        
        val matchCount = items.sumOf { item ->
            itemKeywords.count { keyword ->
                item.contains(keyword, ignoreCase = true)
            }
        }
        
        return if (items.isNotEmpty()) {
            matchCount.toFloat() / (items.size * itemKeywords.size.coerceAtLeast(1))
        } else {
            0.0f
        }
    }
    
    /**
     * Scores based on overall context and additional text
     */
    private fun getContextScore(allText: String, category: ReceiptCategory): Float {
        val contextKeywords = when (category) {
            ReceiptCategory.GROCERIES -> listOf(
                "grocery", "supermarket", "fresh", "organic", "produce", "deli",
                "bakery", "dairy", "frozen", "canned goods", "checkout"
            )
            ReceiptCategory.DINING -> listOf(
                "restaurant", "cafe", "dine in", "take out", "delivery", "tip",
                "server", "table", "order", "menu", "kitchen", "chef"
            )
            ReceiptCategory.TRANSPORTATION -> listOf(
                "station", "pump", "gallon", "liter", "mileage", "vehicle",
                "license", "registration", "inspection", "emissions"
            )
            ReceiptCategory.ELECTRONICS -> listOf(
                "warranty", "tech support", "installation", "upgrade", "software",
                "hardware", "digital", "wireless", "bluetooth", "wifi"
            )
            ReceiptCategory.CLOTHING -> listOf(
                "size", "color", "fashion", "style", "brand", "designer",
                "season", "collection", "fitting", "alteration", "return"
            )
            ReceiptCategory.HEALTHCARE -> listOf(
                "health", "medical", "doctor", "nurse", "patient", "treatment",
                "diagnosis", "symptom", "medication", "dosage", "prescription"
            )
            ReceiptCategory.ENTERTAINMENT -> listOf(
                "entertainment", "fun", "leisure", "hobby", "recreation",
                "performance", "show", "event", "ticket", "admission"
            )
            ReceiptCategory.HOME_GARDEN -> listOf(
                "home improvement", "renovation", "decoration", "interior",
                "exterior", "landscape", "gardening", "lawn care", "maintenance"
            )
            ReceiptCategory.AUTOMOTIVE -> listOf(
                "automotive", "vehicle", "car care", "maintenance", "repair",
                "service", "mechanic", "garage", "dealership", "parts"
            )
            ReceiptCategory.BUSINESS -> listOf(
                "business", "office", "professional", "corporate", "company",
                "organization", "meeting", "conference", "presentation"
            )
            ReceiptCategory.TRAVEL -> listOf(
                "travel", "vacation", "trip", "journey", "destination",
                "booking", "reservation", "check-in", "check-out", "luggage"
            )
            ReceiptCategory.EDUCATION -> listOf(
                "education", "learning", "study", "academic", "school",
                "university", "college", "course", "degree", "certification"
            )
            ReceiptCategory.UTILITIES -> listOf(
                "utility", "service", "monthly", "bill", "account", "usage",
                "meter", "connection", "provider", "customer"
            )
            ReceiptCategory.MISCELLANEOUS -> listOf()
        }
        
        val matchCount = contextKeywords.count { keyword ->
            allText.contains(keyword, ignoreCase = true)
        }
        
        return matchCount.toFloat() / contextKeywords.size.coerceAtLeast(1)
    }
    
    /**
     * Gets detailed category information with confidence
     */
    fun getDetailedCategorization(receiptData: ReceiptData): CategoryResult {
        val merchantName = receiptData.merchantName?.lowercase() ?: ""
        val items = receiptData.items.map { it.name.lowercase() }
        val allText = receiptData.rawText.lowercase()
        
        val categoryScores = ReceiptCategory.values().associateWith { category ->
            calculateCategoryScore(merchantName, items, allText, category)
        }.toList().sortedByDescending { it.second }
        
        return CategoryResult(
            primaryCategory = categoryScores.first().first,
            confidence = categoryScores.first().second,
            alternativeCategories = categoryScores.drop(1).take(3).map {
                AlternativeCategory(it.first, it.second)
            }
        )
    }
}

/**
 * Comprehensive receipt category enumeration
 */
enum class ReceiptCategory(val displayName: String, val color: String) {
    GROCERIES("Groceries", "#4CAF50"),
    DINING("Dining & Restaurants", "#FF9800"),
    TRANSPORTATION("Transportation", "#2196F3"),
    ELECTRONICS("Electronics", "#9C27B0"),
    CLOTHING("Clothing & Fashion", "#E91E63"),
    HEALTHCARE("Healthcare", "#F44336"),
    ENTERTAINMENT("Entertainment", "#FF5722"),
    HOME_GARDEN("Home & Garden", "#795548"),
    AUTOMOTIVE("Automotive", "#607D8B"),
    BUSINESS("Business", "#3F51B5"),
    TRAVEL("Travel", "#009688"),
    EDUCATION("Education", "#FFC107"),
    UTILITIES("Utilities", "#8BC34A"),
    MISCELLANEOUS("Miscellaneous", "#9E9E9E");
    
    companion object {
        fun fromString(category: String): ReceiptCategory {
            return values().find { 
                it.displayName.equals(category, ignoreCase = true) || 
                it.name.equals(category, ignoreCase = true) 
            } ?: MISCELLANEOUS
        }
    }
}

/**
 * Result of categorization with confidence and alternatives
 */
data class CategoryResult(
    val primaryCategory: ReceiptCategory,
    val confidence: Float,
    val alternativeCategories: List<AlternativeCategory>
)

data class AlternativeCategory(
    val category: ReceiptCategory,
    val confidence: Float
)
