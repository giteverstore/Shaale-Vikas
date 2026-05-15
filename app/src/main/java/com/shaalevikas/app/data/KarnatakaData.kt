package com.shaalevikas.app.data

object KarnatakaData {

    val districts = listOf(
        "Bagalkot",
        "Ballari",
        "Belagavi",
        "Bengaluru Rural",
        "Bengaluru Urban",
        "Bidar",
        "Chamarajanagar",
        "Chikkaballapur",
        "Chikkamagaluru",
        "Chitradurga",
        "Dakshina Kannada",
        "Davanagere",
        "Dharwad",
        "Gadag",
        "Hassan",
        "Haveri",
        "Kalaburagi",
        "Kodagu",
        "Kolar",
        "Koppal",
        "Mandya",
        "Mysuru",
        "Raichur",
        "Ramanagara",
        "Shivamogga",
        "Tumakuru",
        "Udupi",
        "Uttara Kannada",
        "Vijayapura",
        "Yadgir"
    )

    val citiesByDistrict = mapOf(
        "Bagalkot" to listOf(
            "Bagalkot", "Mudhol", "Ilkal",
            "Rabkavi Banhatti", "Jamkhandi", "Badami",
            "Guledgudda", "Hungund"
        ),
        "Ballari" to listOf(
            "Ballari", "Hospet", "Siruguppa",
            "Sandur", "Kudligi", "Hagaribommanahalli",
            "Kampli"
        ),
        "Belagavi" to listOf(
            "Belagavi", "Gokak", "Bailhongal",
            "Chikodi", "Raybag", "Athani",
            "Hukkeri", "Ramdurg", "Saundatti",
            "Khanapur"
        ),
        "Bengaluru Rural" to listOf(
            "Doddaballapur", "Nelamangala",
            "Devanahalli", "Hoskote",
            "Ramanagara"
        ),
        "Bengaluru Urban" to listOf(
            "Bengaluru", "Anekal", "Rajajinagar",
            "Yeshwanthpur", "Jayanagar", "Hebbal",
            "Whitefield", "Electronic City",
            "Koramangala", "Indiranagar",
            "Marathahalli", "Bannerghatta"
        ),
        "Bidar" to listOf(
            "Bidar", "Basavakalyan", "Bhalki",
            "Humnabad", "Aurad", "Chitguppa"
        ),
        "Chamarajanagar" to listOf(
            "Chamarajanagar", "Kollegal",
            "Gundlupet", "Yelandur", "Hanur"
        ),
        "Chikkaballapur" to listOf(
            "Chikkaballapur", "Gudibande",
            "Chintamani", "Sidlaghatta",
            "Bagepalli", "Gowribidanur"
        ),
        "Chikkamagaluru" to listOf(
            "Chikkamagaluru", "Kadur", "Tarikere",
            "Mudigere", "Koppa", "Sringeri",
            "Narasimharajapura"
        ),
        "Chitradurga" to listOf(
            "Chitradurga", "Davangere",
            "Hiriyur", "Hosadurga",
            "Holalkere", "Challakere"
        ),
        "Dakshina Kannada" to listOf(
            "Mangaluru", "Puttur", "Bantwal",
            "Belthangady", "Sullia",
            "Moodabidri", "Ullal"
        ),
        "Davanagere" to listOf(
            "Davanagere", "Harihara",
            "Harihar", "Jagaluru",
            "Honnali", "Channagiri"
        ),
        "Dharwad" to listOf(
            "Dharwad", "Hubballi", "Kalghatgi",
            "Kundgol", "Navalgund"
        ),
        "Gadag" to listOf(
            "Gadag", "Betageri", "Ron",
            "Mundargi", "Nargund", "Shirhatti"
        ),
        "Hassan" to listOf(
            "Hassan", "Arsikere", "Channarayapatna",
            "Belur", "Sakleshpur",
            "Alur", "Holenarasipur"
        ),
        "Haveri" to listOf(
            "Haveri", "Ranebennur", "Byadagi",
            "Hirekerur", "Savanur",
            "Shiggaon", "Hanagal"
        ),
        "Kalaburagi" to listOf(
            "Kalaburagi", "Yadgir", "Sedam",
            "Chincholi", "Afzalpur",
            "Aland", "Shorapur"
        ),
        "Kodagu" to listOf(
            "Madikeri", "Somwarpet",
            "Virajpet", "Kushalnagar",
            "Gonikoppal"
        ),
        "Kolar" to listOf(
            "Kolar", "Kolar Gold Fields",
            "Mulbagal", "Bangarpet",
            "Malur", "Srinivaspur"
        ),
        "Koppal" to listOf(
            "Koppal", "Gangavathi",
            "Yelburga", "Kushtagi"
        ),
        "Mandya" to listOf(
            "Mandya", "Maddur", "Malavalli",
            "Srirangapatna", "Nagamangala",
            "Krishnarajapete", "Pandavapura"
        ),
        "Mysuru" to listOf(
            "Mysuru", "Nanjangud", "Hunsur",
            "Kollegal", "Piriyapatna",
            "Krishnarajanagara", "Heggadadevankote",
            "T Narasipura"
        ),
        "Raichur" to listOf(
            "Raichur", "Manvi", "Devadurga",
            "Sindhanur", "Lingsugur",
            "Mudgal"
        ),
        "Ramanagara" to listOf(
            "Ramanagara", "Channapatna",
            "Kanakapura", "Magadi"
        ),
        "Shivamogga" to listOf(
            "Shivamogga", "Bhadravati",
            "Sagar", "Shikaripura",
            "Soraba", "Hosanagara",
            "Tirthahalli"
        ),
        "Tumakuru" to listOf(
            "Tumakuru", "Tiptur", "Sira",
            "Madhugiri", "Kunigal",
            "Koratagere", "Gubbi",
            "Pavagada", "Turuvekere"
        ),
        "Udupi" to listOf(
            "Udupi", "Manipal", "Kundapura",
            "Karkala", "Brahmavar",
            "Byndoor"
        ),
        "Uttara Kannada" to listOf(
            "Karwar", "Sirsi", "Dandeli",
            "Kumta", "Honavar",
            "Bhatkal", "Ankola",
            "Haliyal", "Mundgod",
            "Yellapur"
        ),
        "Vijayapura" to listOf(
            "Vijayapura", "Bijapur",
            "Sindagi", "Basavana Bagewadi",
            "Muddebihal", "Indi",
            "Chadchan"
        ),
        "Yadgir" to listOf(
            "Yadgir", "Shorapur",
            "Shahapur", "Gurmitkal",
            "Wadagera"
        )
    )
}