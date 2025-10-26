import java.util.*;

public class ApiEndpoint {
    Map<String, String> memberCode = new HashMap<>();

{
    memberCode.put("SG", "C702");
    memberCode.put("Singapore" , "C702");

    memberCode.put("US" , "C840");
    memberCode.put("United States" , "C840");

    memberCode.put("MY" , "C458");
    memberCode.put("Malaysia" , "C458");

    memberCode.put("TH" , "C764");
    memberCode.put("Thailand" , "C764");

    memberCode.put("VN" , "C704");
    memberCode.put("Vietnam" , "C704");

    memberCode.put("ID" , "C360");
    memberCode.put("Indonesia" , "C360");

    memberCode.put("PH" , "C608");
    memberCode.put("Philippines" , "C608");

    memberCode.put("KR" , "C410");
    memberCode.put("South Korea" , "C410");

    memberCode.put("IN" , "C356");
    memberCode.put("India" , "C356");

    memberCode.put("AU" , "C036");
    memberCode.put("Australia" , "C036");

    memberCode.put("GB" , "C826");
    memberCode.put("United Kingdom" , "C826");

    memberCode.put("DE" , "U918"); //European Union
    memberCode.put("Germany" , "U918"); //European Union
    memberCode.put("FR" , "U918");//European Union , can't find the "german" and "france" Econnomies in website, doesnt exist
    memberCode.put("France" , "U918");// EU
    memberCode.put("IT" , "U918"); //EU
    memberCode.put("Italy" , "U918"); //EU
    memberCode.put("ES" , "U918"); //EU
    memberCode.put("Spain" , "U918"); //EU

    memberCode.put("CA" , "C124");
    memberCode.put("Canada" , "C124");

    memberCode.put("BR" , "C076");
    memberCode.put("Brazil" , "C076");

    memberCode.put("MX" , "C484");
    memberCode.put("Mexico" , "C484");

    memberCode.put("RU" , "C643");
    memberCode.put("Russia" , "C643");

    memberCode.put("ZA" , "C710");
    memberCode.put("South Africa" , "C710");

    memberCode.put("CN" , "C156");
    memberCode.put("China" , "C156");

    memberCode.put("JP" , "C392");
    memberCode.put("Japan" , "C392");

}

    Map<String,String> productCodeSG = new HashMap<>();
    {
        productCode.put("electronics" , "85423900");//there are alot of different tariffs with specific details
        productCode.put("Electronics" , "85423900");//i chose the one with processors, memories, computer parts stuff

        productCode.put("clothing" , "62099000");
        productCode.put("Clothing" , "62099000");//babies garments and clothing accesories


        productCode.put("furniture" , "94031000"); //Metal funriture for offices, assuming the customers are companies
        productCode.put("Furniture" , "9403100");//buying furnitue


        productCode.put("food" , "16010010");//sausages and similar meats, the others are specific into the kind of
        productCode.put("Food" , "16010010");//meat such as chicken, hence i just chose the one with "vague" meat


        productCode.put("books" , "49011000");//I am assuming printed books
        productCode.put("Books" , "49011000");

        productCode.put("toys" , "95030010");//assuming wheeled toys, recreational models and puzzles
        productCode.put("Toys" , "95030010");

        productCode.put("tools" , "82055100");//assuming household tools
        productCode.put("Tools" , "82055100");

        productCode.put("beauty" , "33049920");
        productCode.put("Beauty Products" , "33049920");

        productCode.put("sports" , "64021910");//assuming sports footwear, the other items like gloves
        productCode.put("Sports Equipment" , "64021910");//would be dependent on the material, so we would be looking at tariff for leather instead of sports for example

        productCode.put("automotive" , "84082010");//looking at engines for diesel vehicles
        productCode.put("Automotive Parts" , "84082010");//when looking at vehicles, its dependent on which part we looking at, chose engine as all vehicles need it

    }

    Map<String,String> productCodeUS = new HashMap<>();
    {
        productCode.put("electronics" , "85423100");//there are alot of different tariffs with specific details
        productCode.put("Electronics" , "85423100");//i chose the one with processors, memories, computer parts stuff

        productCode.put("clothing" , "62099005");
        productCode.put("Clothing" , "62099005");//babies garments and clothing accesories


        productCode.put("furniture" , "94031000"); //Metal funriture for offices, assuming the customers are companies
        productCode.put("Furniture" , "94031000");//buying furnitue


        productCode.put("food" , "16010020");//sausages and similar meats, the others are specific into the kind of
        productCode.put("Food" , "16010020");//meat such as chicken, hence i just chose the one with "vague" meat


        productCode.put("books" , "49011000");//I am assuming printed books
        productCode.put("Books" , "49011000");

        productCode.put("toys" , "95030000");//assuming wheeled toys, recreational models and puzzles
        productCode.put("Toys" , "95030000");

        productCode.put("tools" , "82055130");//assuming household tools
        productCode.put("Tools" , "82055130");

        productCode.put("beauty" , "33049910");
        productCode.put("Beauty Products" , "33049910");

        productCode.put("sports" , "64021905");//assuming sports footwear, the other items like gloves
        productCode.put("Sports Equipment" , "64021905");//would be dependent on the material, so we would be looking at tariff for leather instead of sports for example

        productCode.put("automotive" , "84082020");//looking at engines for diesel vehicles
        productCode.put("Automotive Parts" , "84082020");//when looking at vehicles, its dependent on which part we looking at, chose engine as all vehicles need it

    }

    Map<String,String> productCodeMY = new HashMap<>();
    {
        productCode.put("electronics" , "8542310000");//there are alot of different tariffs with specific details
        productCode.put("Electronics" , "8542310000");//i chose the one with processors, memories, computer parts stuff

        productCode.put("clothing" , "6209900000");
        productCode.put("Clothing" , "6209900000");//babies garments and clothing accesories


        productCode.put("furniture" , "9403100000"); //Metal funriture for offices, assuming the customers are companies
        productCode.put("Furniture" , "9403100000");//buying furnitue


        productCode.put("food" , "1601001000");//sausages and similar meats, the others are specific into the kind of
        productCode.put("Food" , "1601001000");//meat such as chicken, hence i just chose the one with "vague" meat


        productCode.put("books" , "4901100000");//I am assuming printed books
        productCode.put("Books" , "4901100000");

        productCode.put("toys" , "9503001000");//assuming wheeled toys, recreational models and puzzles
        productCode.put("Toys" , "9503001000");

        productCode.put("tools" , "8205510000");//assuming household tools
        productCode.put("Tools" , "8205510000");

        productCode.put("beauty" , "3304993000");
        productCode.put("Beauty Products" , "3304993000");

        productCode.put("sports" , "6402191000");//assuming sports footwear, the other items like gloves
        productCode.put("Sports Equipment" , "6402191000");//would be dependent on the material, so we would be looking at tariff for leather instead of sports for example

        productCode.put("automotive" , "8408202100");//looking at engines for diesel vehicles
        productCode.put("Automotive Parts" , "8408202100");//when looking at vehicles, its dependent on which part we looking at, chose engine as all vehicles need it

    }

    Map<String,String> productCodeTH = new HashMap<>();
    {
        productCode.put("electronics" , "85423100");//there are alot of different tariffs with specific details
        productCode.put("Electronics" , "85423100");//i chose the one with processors, memories, computer parts stuff

        productCode.put("clothing" , "62099000");
        productCode.put("Clothing" , "62099000");//babies garments and clothing accesories


        productCode.put("furniture" , "94031000"); //Metal funriture for offices, assuming the customers are companies
        productCode.put("Furniture" , "94031000");//buying furnitue


        productCode.put("food" , "16010010");//sausages and similar meats, the others are specific into the kind of
        productCode.put("Food" , "16010010");//meat such as chicken, hence i just chose the one with "vague" meat


        productCode.put("books" , "49011000");//I am assuming printed books
        productCode.put("Books" , "49011000");

        productCode.put("toys" , "95030010");//assuming wheeled toys, recreational models and puzzles
        productCode.put("Toys" , "95030010");

        productCode.put("tools" , "82055100");//assuming household tools
        productCode.put("Tools" , "82055100");

        productCode.put("beauty" , "33049930");
        productCode.put("Beauty Products" , "33049930");

        productCode.put("sports" , "64021990");//assuming sports footwear, the other items like gloves
        productCode.put("Sports Equipment" , "64021990");//would be dependent on the material, so we would be looking at tariff for leather instead of sports for example

        productCode.put("automotive" , "84082021");//looking at engines for diesel vehicles
        productCode.put("Automotive Parts" , "84082021");//when looking at vehicles, its dependent on which part we looking at, chose engine as all vehicles need it

    }

    Map<String,String> productCodeVN = new HashMap<>();
    {
        productCode.put("electronics" , "8542310000");//there are alot of different tariffs with specific details
        productCode.put("Electronics" , "8542310000");//i chose the one with processors, memories, computer parts stuff

        productCode.put("clothing" , "6209900000");
        productCode.put("Clothing" , "6209900000");//babies garments and clothing accesories


        productCode.put("furniture" , "9403100000"); //Metal funriture for offices, assuming the customers are companies
        productCode.put("Furniture" , "9403100000");//buying furnitue


        productCode.put("food" , "1601001010");//sausages and similar meats, the others are specific into the kind of
        productCode.put("Food" , "1601001010");//meat such as chicken, hence i just chose the one with "vague" meat


        productCode.put("books" , "4901100000");//I am assuming printed books
        productCode.put("Books" , "4901100000");

        productCode.put("toys" , "9503001000");//assuming wheeled toys, recreational models and puzzles
        productCode.put("Toys" , "9503001000");

        productCode.put("tools" , "8205510000");//assuming household tools
        productCode.put("Tools" , "8205510000");

        productCode.put("beauty" , "3304993000");
        productCode.put("Beauty Products" , "3304993000");

        productCode.put("sports" , "6402199000");//assuming sports footwear, the other items like gloves
        productCode.put("Sports Equipment" , "6402199000");//would be dependent on the material, so we would be looking at tariff for leather instead of sports for example

        productCode.put("automotive" , "8408202120");//looking at engines for diesel vehicles
        productCode.put("Automotive Parts" , "8408202120");//when looking at vehicles, its dependent on which part we looking at, chose engine as all vehicles need it

    }


    Map<String,String> productCodeID = new HashMap<>();
    {
        productCode.put("electronics" , "85423100");//there are alot of different tariffs with specific details
        productCode.put("Electronics" , "85423100");//i chose the one with processors, memories, computer parts stuff

        productCode.put("clothing" , "62099000");
        productCode.put("Clothing" , "62099000");//babies garments and clothing accesories


        productCode.put("furniture" , "94031000"); //Metal funriture for offices, assuming the customers are companies
        productCode.put("Furniture" , "94031000");//buying furnitue


        productCode.put("food" , "16010010");//sausages and similar meats, the others are specific into the kind of
        productCode.put("Food" , "16010010");//meat such as chicken, hence i just chose the one with "vague" meat


        productCode.put("books" , "49011000");//I am assuming printed books
        productCode.put("Books" , "49011000");

        productCode.put("toys" , "95030010");//assuming wheeled toys, recreational models and puzzles
        productCode.put("Toys" , "95030010");

        productCode.put("tools" , "82055100");//assuming household tools
        productCode.put("Tools" , "82055100");

        productCode.put("beauty" , "33049930");
        productCode.put("Beauty Products" , "33049930");

        productCode.put("sports" , "64021990");//assuming sports footwear, the other items like gloves
        productCode.put("Sports Equipment" , "64021990");//would be dependent on the material, so we would be looking at tariff for leather instead of sports for example

        productCode.put("automotive" , "84082021");//looking at engines for diesel vehicles
        productCode.put("Automotive Parts" , "84082021");//when looking at vehicles, its dependent on which part we looking at, chose engine as all vehicles need it

    }


    Map<String,String> productCodePH = new HashMap<>();
    {
        productCode.put("electronics" , "85423100");//there are alot of different tariffs with specific details
        productCode.put("Electronics" , "85423100");//i chose the one with processors, memories, computer parts stuff

        productCode.put("clothing" , "62099000");
        productCode.put("Clothing" , "62099000");//babies garments and clothing accesories


        productCode.put("furniture" , "94031000"); //Metal funriture for offices, assuming the customers are companies
        productCode.put("Furniture" , "94031000");//buying furnitue


        productCode.put("food" , "16010010");//sausages and similar meats, the others are specific into the kind of
        productCode.put("Food" , "16010010");//meat such as chicken, hence i just chose the one with "vague" meat


        productCode.put("books" , "49011000");//I am assuming printed books
        productCode.put("Books" , "49011000");

        productCode.put("toys" , "95030010");//assuming wheeled toys, recreational models and puzzles
        productCode.put("Toys" , "95030010");

        productCode.put("tools" , "82055100");//assuming household tools
        productCode.put("Tools" , "82055100");

        productCode.put("beauty" , "33049930");
        productCode.put("Beauty Products" , "33049930");

        productCode.put("sports" , "64021990");//assuming sports footwear, the other items like gloves
        productCode.put("Sports Equipment" , "64021990");//would be dependent on the material, so we would be looking at tariff for leather instead of sports for example

        productCode.put("automotive" , "84082021");//looking at engines for diesel vehicles
        productCode.put("Automotive Parts" , "84082021");//when looking at vehicles, its dependent on which part we looking at, chose engine as all vehicles need it

    }

    Map<String,String> productCodeKR = new HashMap<>();
    {
        productCode.put("electronics" , "8542313000");//there are alot of different tariffs with specific details
        productCode.put("Electronics" , "8542313000");//i chose the one with processors, memories, computer parts stuff

        productCode.put("clothing" , "6209901000");
        productCode.put("Clothing" , "6209901000");//babies garments and clothing accesories


        productCode.put("furniture" , "9403100000"); //Metal funriture for offices, assuming the customers are companies
        productCode.put("Furniture" , "9403100000");//buying furnitue


        productCode.put("food" , "1601009000");//sausages and similar meats, the others are specific into the kind of
        productCode.put("Food" , "1601009000");//meat such as chicken, hence i just chose the one with "vague" meat


        productCode.put("books" , "4901101000");//I am assuming printed books
        productCode.put("Books" , "4901101000");

        productCode.put("toys" , "9503001900");//assuming wheeled toys, recreational models and puzzles
        productCode.put("Toys" , "9503001900");

        productCode.put("tools" , "8205510000");//assuming household tools
        productCode.put("Tools" , "8205510000");

        productCode.put("beauty" , "3304991000");
        productCode.put("Beauty Products" , "3304991000");

        productCode.put("sports" , "6402190000");//assuming sports footwear, the other items like gloves
        productCode.put("Sports Equipment" , "6402190000");//would be dependent on the material, so we would be looking at tariff for leather instead of sports for example

        productCode.put("automotive" , "8408202000");//looking at engines for diesel vehicles
        productCode.put("Automotive Parts" , "8408202000");//when looking at vehicles, its dependent on which part we looking at, chose engine as all vehicles need it

    }

    Map<String,String> productCodeIN = new HashMap<>();
    {
        productCode.put("electronics" , "85423100");//there are alot of different tariffs with specific details
        productCode.put("Electronics" , "85423100");//i chose the one with processors, memories, computer parts stuff

        productCode.put("clothing" , "62099010");
        productCode.put("Clothing" , "62099010");//babies garments and clothing accesories


        productCode.put("furniture" , "94031010"); //Metal funriture for offices, assuming the customers are companies
        productCode.put("Furniture" , "94031010");//buying furnitue


        productCode.put("food" , "16010000");//sausages and similar meats, the others are specific into the kind of
        productCode.put("Food" , "16010000");//meat such as chicken, hence i just chose the one with "vague" meat


        productCode.put("books" , "49011010");//I am assuming printed books
        productCode.put("Books" , "49011010");

        productCode.put("toys" , "95030010");//assuming wheeled toys, recreational models and puzzles
        productCode.put("Toys" , "95030010");

        productCode.put("tools" , "82055190");//assuming household tools
        productCode.put("Tools" , "82055190");

        productCode.put("beauty" , "33049910");
        productCode.put("Beauty Products" , "33049910");

        productCode.put("sports" , "64021910");//assuming sports footwear, the other items like gloves
        productCode.put("Sports Equipment" , "64021910");//would be dependent on the material, so we would be looking at tariff for leather instead of sports for example

        productCode.put("automotive" , "84082010");//looking at engines for diesel vehicles
        productCode.put("Automotive Parts" , "84082010");//when looking at vehicles, its dependent on which part we looking at, chose engine as all vehicles need it

    }

    Map<String,String> productCodeAU = new HashMap<>();
    {
        productCode.put("electronics" , "85423100");//there are alot of different tariffs with specific details
        productCode.put("Electronics" , "85423100");//i chose the one with processors, memories, computer parts stuff

        productCode.put("clothing" , "62099010");
        productCode.put("Clothing" , "62099010");//babies garments and clothing accesories


        productCode.put("furniture" , "94031000"); //Metal funriture for offices, assuming the customers are companies
        productCode.put("Furniture" , "94031000");//buying furnitue


        productCode.put("food" , "16010000");//sausages and similar meats, the others are specific into the kind of
        productCode.put("Food" , "16010000");//meat such as chicken, hence i just chose the one with "vague" meat


        productCode.put("books" , "49011000");//I am assuming printed books
        productCode.put("Books" , "49011000");

        productCode.put("toys" , "95030050");//assuming wheeled toys, recreational models and puzzles
        productCode.put("Toys" , "95030050");

        productCode.put("tools" , "82055100");//assuming household tools
        productCode.put("Tools" , "82055100");

        productCode.put("beauty" , "33049900");
        productCode.put("Beauty Products" , "33049900");//????

        productCode.put("sports" , "64021900");//assuming sports footwear, the other items like gloves
        productCode.put("Sports Equipment" , "64021900");//would be dependent on the material, so we would be looking at tariff for leather instead of sports for example

        productCode.put("automotive" , "84082010");//looking at engines for diesel vehicles
        productCode.put("Automotive Parts" , "84082010");//when looking at vehicles, its dependent on which part we looking at, chose engine as all vehicles need it

    }


    Map<String,String> productCodeGB = new HashMap<>();
    {
        productCode.put("electronics" , "85423190");//there are alot of different tariffs with specific details
        productCode.put("Electronics" , "85423190");//i chose the one with processors, memories, computer parts stuff

        productCode.put("clothing" , "62099010");
        productCode.put("Clothing" , "62099010");//babies garments and clothing accesories


        productCode.put("furniture" , "94031051"); //Metal funriture for offices, assuming the customers are companies
        productCode.put("Furniture" , "94031051");//buying furnitue


        productCode.put("food" , "16010010");//sausages and similar meats, the others are specific into the kind of
        productCode.put("Food" , "16010010");//meat such as chicken, hence i just chose the one with "vague" meat


        productCode.put("books" , "49011000");//I am assuming printed books
        productCode.put("Books" , "49011000");

        productCode.put("toys" , "95030055");//assuming wheeled toys, recreational models and puzzles
        productCode.put("Toys" , "95030055");

        productCode.put("tools" , "82055100");//assuming household tools
        productCode.put("Tools" , "82055100");

        productCode.put("beauty" , "33049900");
        productCode.put("Beauty Products" , "33049900");

        productCode.put("sports" , "64021900");//assuming sports footwear, the other items like gloves
        productCode.put("Sports Equipment" , "64021900");//would be dependent on the material, so we would be looking at tariff for leather instead of sports for example

        productCode.put("automotive" , "84082010");//looking at engines for diesel vehicles
        productCode.put("Automotive Parts" , "84082010");//when looking at vehicles, its dependent on which part we looking at, chose engine as all vehicles need it

    }


    Map<String,String> productCodeEU = new HashMap<>();
    {
        productCode.put("electronics" , "85423190");//there are alot of different tariffs with specific details
        productCode.put("Electronics" , "85423190");//i chose the one with processors, memories, computer parts stuff

        productCode.put("clothing" , "62099090");
        productCode.put("Clothing" , "62099090");//babies garments and clothing accesories


        productCode.put("furniture" , "94031051"); //Metal funriture for offices, assuming the customers are companies
        productCode.put("Furniture" , "94031051");//buying furnitue


        productCode.put("food" , "16010099");//sausages and similar meats, the others are specific into the kind of
        productCode.put("Food" , "16010099");//meat such as chicken, hence i just chose the one with "vague" meat


        productCode.put("books" , "49011000");//I am assuming printed books
        productCode.put("Books" , "49011000");

        productCode.put("toys" , "95030039");//assuming wheeled toys, recreational models and puzzles
        productCode.put("Toys" , "95030039");

        productCode.put("tools" , "82055100");//assuming household tools
        productCode.put("Tools" , "82055100");

        productCode.put("beauty" , "33049900");
        productCode.put("Beauty Products" , "33049900");

        productCode.put("sports" , "64021900");//assuming sports footwear, the other items like gloves
        productCode.put("Sports Equipment" , "64021900");//would be dependent on the material, so we would be looking at tariff for leather instead of sports for example

        productCode.put("automotive" , "84082010");//looking at engines for diesel vehicles
        productCode.put("Automotive Parts" , "84082010");//when looking at vehicles, its dependent on which part we looking at, chose engine as all vehicles need it

    }

    Map<String,String> productCodeCA = new HashMap<>();
    {
        productCode.put("electronics" , "85423100");//there are alot of different tariffs with specific details
        productCode.put("Electronics" , "85423100");//i chose the one with processors, memories, computer parts stuff

        productCode.put("clothing" , "62099000");
        productCode.put("Clothing" , "62099000");//babies garments and clothing accesories


        productCode.put("furniture" , "94031000"); //Metal funriture for offices, assuming the customers are companies
        productCode.put("Furniture" , "94031000");//buying furnitue


        productCode.put("food" , "16010090");//sausages and similar meats, the others are specific into the kind of
        productCode.put("Food" , "16010090");//meat such as chicken, hence i just chose the one with "vague" meat


        productCode.put("books" , "49011000");//I am assuming printed books
        productCode.put("Books" , "49011000");

        productCode.put("toys" , "95030010");//assuming wheeled toys, recreational models and puzzles
        productCode.put("Toys" , "95030010");

        productCode.put("tools" , "82055190");//assuming household tools
        productCode.put("Tools" , "82055190");

        productCode.put("beauty" , "33049990");
        productCode.put("Beauty Products" , "33049990");

        productCode.put("sports" , "64021910");//assuming sports footwear, the other items like gloves
        productCode.put("Sports Equipment" , "64021910");//would be dependent on the material, so we would be looking at tariff for leather instead of sports for example

        productCode.put("automotive" , "84082000");//looking at engines for diesel vehicles
        productCode.put("Automotive Parts" , "84082000");//when looking at vehicles, its dependent on which part we looking at, chose engine as all vehicles need it

    }

    Map<String,String> productCodeBR = new HashMap<>();
    {
        productCode.put("electronics" , "85423190");//there are alot of different tariffs with specific details
        productCode.put("Electronics" , "85423190");//i chose the one with processors, memories, computer parts stuff

        productCode.put("clothing" , "62099010");
        productCode.put("Clothing" , "62099010");//babies garments and clothing accesories


        productCode.put("furniture" , "94031000"); //Metal funriture for offices, assuming the customers are companies
        productCode.put("Furniture" , "94031000");//buying furnitue


        productCode.put("food" , "16010000");//sausages and similar meats, the others are specific into the kind of
        productCode.put("Food" , "16010000");//meat such as chicken, hence i just chose the one with "vague" meat


        productCode.put("books" , "49011000");//I am assuming printed books
        productCode.put("Books" , "49011000");

        productCode.put("toys" , "95030010");//assuming wheeled toys, recreational models and puzzles
        productCode.put("Toys" , "95030010");

        productCode.put("tools" , "82055100");//assuming household tools
        productCode.put("Tools" , "82055100");

        productCode.put("beauty" , "33049910");
        productCode.put("Beauty Products" , "33049910");

        productCode.put("sports" , "64021900");//assuming sports footwear, the other items like gloves
        productCode.put("Sports Equipment" , "64021900");//would be dependent on the material, so we would be looking at tariff for leather instead of sports for example

        productCode.put("automotive" , "84082010");//looking at engines for diesel vehicles
        productCode.put("Automotive Parts" , "84082010");//when looking at vehicles, its dependent on which part we looking at, chose engine as all vehicles need it

    }


    Map<String,String> productCodeMX = new HashMap<>();
    {
        productCode.put("electronics" , "85423103");//there are alot of different tariffs with specific details
        productCode.put("Electronics" , "85423103");//i chose the one with processors, memories, computer parts stuff

        productCode.put("clothing" , "62099091");
        productCode.put("Clothing" , "62099091");//babies garments and clothing accesories


        productCode.put("furniture" , "94031003"); //Metal funriture for offices, assuming the customers are companies
        productCode.put("Furniture" , "94031003");//buying furnitue


        productCode.put("food" , "16010003");//sausages and similar meats, the others are specific into the kind of
        productCode.put("Food" , "16010003");//meat such as chicken, hence i just chose the one with "vague" meat


        productCode.put("books" , "49011001");//I am assuming printed books
        productCode.put("Books" , "49011001");

        productCode.put("toys" , "95030002");//assuming wheeled toys, recreational models and puzzles
        productCode.put("Toys" , "95030002");

        productCode.put("tools" , "82055199");//assuming household tools
        productCode.put("Tools" , "82055199");

        productCode.put("beauty" , "33049101");
        productCode.put("Beauty Products" , "33049101");

        productCode.put("sports" , "64021901");//assuming sports footwear, the other items like gloves
        productCode.put("Sports Equipment" , "64021901");//would be dependent on the material, so we would be looking at tariff for leather instead of sports for example

        productCode.put("automotive" , "84082001");//looking at engines for diesel vehicles
        productCode.put("Automotive Parts" , "84082001");//when looking at vehicles, its dependent on which part we looking at, chose engine as all vehicles need it

    }



    Map<String,String> productCodeRU = new HashMap<>();
    {
        productCode.put("electronics" , "8542311001");//there are alot of different tariffs with specific details
        productCode.put("Electronics" , "8542311001");//i chose the one with processors, memories, computer parts stuff

        productCode.put("clothing" , "6209909000");
        productCode.put("Clothing" , "6209909000");//babies garments and clothing accesories


        productCode.put("furniture" , "9403105100"); //Metal funriture for offices, assuming the customers are companies
        productCode.put("Furniture" , "9403105100");//buying furnitue


        productCode.put("food" , "1601009100");//sausages and similar meats, the others are specific into the kind of
        productCode.put("Food" , "1601009100");//meat such as chicken, hence i just chose the one with "vague" meat


        productCode.put("books" , "4901100000");//I am assuming printed books
        productCode.put("Books" , "4901100000");

        productCode.put("toys" , "9503003500");//assuming wheeled toys, recreational models and puzzles
        productCode.put("Toys" , "9503003500");

        productCode.put("tools" , "8205510090");//assuming household tools
        productCode.put("Tools" , "8205510090");

        productCode.put("beauty" , "3304990000");
        productCode.put("Beauty Products" , "3304990000");

        productCode.put("sports" , "6402190000");//assuming sports footwear, the other items like gloves
        productCode.put("Sports Equipment" , "6402190000");//would be dependent on the material, so we would be looking at tariff for leather instead of sports for example

        productCode.put("automotive" , "8408203109");//looking at engines for diesel vehicles
        productCode.put("Automotive Parts" , "8408203109");//when looking at vehicles, its dependent on which part we looking at, chose engine as all vehicles need it

    }


    Map<String,String> productCodeZA = new HashMap<>();
    {
        productCode.put("electronics" , "85423100");//there are alot of different tariffs with specific details
        productCode.put("Electronics" , "85423100");//i chose the one with processors, memories, computer parts stuff

        productCode.put("clothing" , "62099000");
        productCode.put("Clothing" , "62099000");//babies garments and clothing accesories


        productCode.put("furniture" , "94031000"); //Metal funriture for offices, assuming the customers are companies
        productCode.put("Furniture" , "94031000");//buying furnitue


        productCode.put("food" , "16010020");//sausages and similar meats, the others are specific into the kind of
        productCode.put("Food" , "16010020");//meat such as chicken, hence i just chose the one with "vague" meat


        productCode.put("books" , "49011000");//I am assuming printed books
        productCode.put("Books" , "49011000");

        productCode.put("toys" , "95030010");//assuming wheeled toys, recreational models and puzzles
        productCode.put("Toys" , "95030010");

        productCode.put("tools" , "82055100");//assuming household tools
        productCode.put("Tools" , "82055100");

        productCode.put("beauty" , "33049920");
        productCode.put("Beauty Products" , "33049920");

        productCode.put("sports" , "64021900");//assuming sports footwear, the other items like gloves
        productCode.put("Sports Equipment" , "64021900");//would be dependent on the material, so we would be looking at tariff for leather instead of sports for example

        productCode.put("automotive" , "84082000");//looking at engines for diesel vehicles
        productCode.put("Automotive Parts" , "84082000");//when looking at vehicles, its dependent on which part we looking at, chose engine as all vehicles need it

    }


    Map<String,String> productCodeCN = new HashMap<>();
    {
        productCode.put("electronics" , "85423111");//there are alot of different tariffs with specific details
        productCode.put("Electronics" , "85423111");//i chose the one with processors, memories, computer parts stuff

        productCode.put("clothing" , "62099010");
        productCode.put("Clothing" , "62099010");//babies garments and clothing accesories


        productCode.put("furniture" , "94031000"); //Metal funriture for offices, assuming the customers are companies
        productCode.put("Furniture" , "94031000");//buying furnitue


        productCode.put("food" , "16010030");//sausages and similar meats, the others are specific into the kind of
        productCode.put("Food" , "16010030");//meat such as chicken, hence i just chose the one with "vague" meat


        productCode.put("books" , "49011000");//I am assuming printed books
        productCode.put("Books" , "49011000");

        productCode.put("toys" , "95030010");//assuming wheeled toys, recreational models and puzzles
        productCode.put("Toys" , "95030010");

        productCode.put("tools" , "82055100");//assuming household tools
        productCode.put("Tools" , "82055100");

        productCode.put("beauty" , "33049900");
        productCode.put("Beauty Products" , "33049900");

        productCode.put("sports" , "64021200");//assuming sports footwear, the other items like gloves
        productCode.put("Sports Equipment" , "64021200");//would be dependent on the material, so we would be looking at tariff for leather instead of sports for example

        productCode.put("automotive" , "84082090");//looking at engines for diesel vehicles
        productCode.put("Automotive Parts" , "84082090");//when looking at vehicles, its dependent on which part we looking at, chose engine as all vehicles need it

    }


    Map<String,String> productCodeJP = new HashMap<>();
    {
        productCode.put("electronics" , "854231039");//there are alot of different tariffs with specific details
        productCode.put("Electronics" , "854231039");//i chose the one with processors, memories, computer parts stuff

        productCode.put("clothing" , "620990150");
        productCode.put("Clothing" , "620990150");//babies garments and clothing accesories


        productCode.put("furniture" , "940310000"); //Metal funriture for offices, assuming the customers are companies
        productCode.put("Furniture" , "940310000");//buying furnitue


        productCode.put("food" , "160100900");//sausages and similar meats, the others are specific into the kind of
        productCode.put("Food" , "160100900");//meat such as chicken, hence i just chose the one with "vague" meat


        productCode.put("books" , "490110000");//I am assuming printed books
        productCode.put("Books" , "490110000");

        productCode.put("toys" , "950300000");//assuming wheeled toys, recreational models and puzzles
        productCode.put("Toys" , "950300000");

        productCode.put("tools" , "820551000");//assuming household tools
        productCode.put("Tools" , "820551000");

        productCode.put("beauty" , "330499010");
        productCode.put("Beauty Products" , "330499010");

        productCode.put("sports" , "640219000");//assuming sports footwear, the other items like gloves
        productCode.put("Sports Equipment" , "640219000");//would be dependent on the material, so we would be looking at tariff for leather instead of sports for example

        productCode.put("automotive" , "840820000");//looking at engines for diesel vehicles
        productCode.put("Automotive Parts" , "840820000");//when looking at vehicles, its dependent on which part we looking at, chose engine as all vehicles need it

    }
//api url is supposed to look like
//ttd.wto.org/en?member={memberCode}&year={year}&product={productCode}
/*
 * for example we want singapore , electroincs , year 2022
 * then url is:
 * 
 * ttd.wto.org/en?member=C702&year=2022&product=85423900
 */

 String url = "https://ttd.wto.org/en?member={member}&year={year}&product={product}";

 Map<String, String> params = new HashMap<>();
 {
params.put("member", "C702");
params.put("year", "2024");
params.put("product", "85423900");
 }

String response = restTemplate.getForObject(url, String.class, params);
}

