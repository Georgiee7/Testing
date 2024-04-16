package me.george.testing.entity.pokemon;

import lombok.Getter;
import me.george.testing.entity.pokemon.stats.PermanentStats;
import me.george.testing.entity.pokemon.types.t1.fire.Charmander;
import me.george.testing.entity.pokemon.stats.PermanentStat;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A registry of all available types of Pokémon.
 * Created by George K. on 4/12/2024.
 */
@Getter
public enum PokemonType {
    VENUSAUR(3, "Venusaur", baseStats(80, 82, 83, 100, 100, 80), 2.0F, 100.0F, null, evolutionsOf(), Element.GRASS, Element.POISON),
    IVYSAUR(2, "Ivysaur", baseStats(60, 62, 63, 80, 80, 60), 1.0F, 13.0F, null, evolutionsOf(VENUSAUR), Element.GRASS, Element.POISON),
    BULBASAUR(1, "Bulbasaur", baseStats(45, 49, 49, 65, 65, 45), 0.7F, 6.9F, null, evolutionsOf(IVYSAUR), Element.GRASS, Element.POISON),

    CHARIZARD(6, "Charizard", baseStats(78, 84, 78, 109, 85, 100), 1.7F, 90F, null, evolutionsOf(), Element.FIRE, Element.DRAGON),
    CHARMANDER(4, "Charmander", baseStats(39, 52, 43, 60, 50, 65), 0.6F, 8.5F, Charmander::new, evolutionsOf(CHARIZARD), Element.FIRE),
    CHARMELEON(5, "Charmeleon", baseStats(58, 64, 58, 80, 65, 80), 1.1F, 19.0F, null, evolutionsOf(CHARIZARD), Element.FIRE),

    BLASTOISE(9, "Blastoise", baseStats(79, 83, 100, 85, 105, 78), 1.6F, 85.5F, null, evolutionsOf(), Element.WATER),
    WARTORTLE(8, "Wartortle", baseStats(59, 63, 80, 65, 80, 58), 1.0F, 22.5F, null, evolutionsOf(BLASTOISE), Element.WATER),
    SQUIRTLE(7, "Squirtle", baseStats(44, 48, 65, 50, 64, 43), 0.5F, 9F, null, evolutionsOf(WARTORTLE), Element.WATER),

    BUTTERFREE(12, "Butterfree", baseStats(60, 45, 50, 90, 80, 70), 1.1F, 32.0F, null, evolutionsOf(), Element.BUG, Element.FLYING),
    METAPOD(11, "Metapod", baseStats(50, 20, 55, 25, 25, 30), 0.7F, 9.9F, null, evolutionsOf(BUTTERFREE), Element.BUG),
    CATERPIE(10, "Caterpie", baseStats(45, 30, 35, 20, 20, 45), 0.3F, 2.9F, null, evolutionsOf(METAPOD), Element.BUG),

    BEEDRILL(15, "Beedrill", baseStats(65, 90, 40, 45, 80, 75), 1.0F, 29.5F, null, evolutionsOf(), Element.BUG, Element.POISON),
    KAKUNA(14, "Kakuna", baseStats(45, 25, 50, 25, 25, 35), 0.6F, 10.0F, null, evolutionsOf(BEEDRILL), Element.BUG, Element.POISON),
    WEEDLE(13, "Weedle", baseStats(40, 35, 30, 20, 20, 50), 0.3F, 3.2F, null, evolutionsOf(KAKUNA), Element.BUG, Element.POISON),

    PIDGEOT(18, "Pidgeot", baseStats(83, 80, 75, 70, 70, 101), 1.5F, 39.5F, null, evolutionsOf(), Element.NORMAL, Element.FLYING),
    PIDGEOTTO(17, "Pidgeotto", baseStats(63, 60, 55, 50, 50, 71), 1.1F, 30.0F, null, evolutionsOf(PIDGEOT), Element.NORMAL, Element.FLYING),
    PIDGEY(16, "Pidgey", baseStats(40, 45, 40, 35, 35, 56), 0.3F, 1.8F, null, evolutionsOf(PIDGEOTTO), Element.NORMAL, Element.FLYING),

    RATICATE(20, "Raticate", baseStats(55, 81, 60, 50, 70, 97), 0.7F, 18.5F, null, evolutionsOf(), Element.NORMAL),
    RATTATA(19, "Rattata", baseStats(30, 56, 35, 25, 35, 72), 0.3F, 3.5F, null, evolutionsOf(RATICATE), Element.NORMAL),

    FEAROW(22, "Fearow", baseStats(65, 90, 65, 61, 61, 100), 1.2F, 38.0F, null, evolutionsOf(), Element.NORMAL, Element.FLYING),
    SPEAROW(21, "Spearow", baseStats(40, 60, 30, 31, 31, 70), 0.3F, 2.0F, null, evolutionsOf(FEAROW), Element.NORMAL, Element.FLYING),

    ARBOK(24, "Arbok", baseStats(60, 85, 69, 65, 79, 80), 3.5F, 65.0F, null, evolutionsOf(), Element.POISON),
    EKANS(23, "Ekans", baseStats(35, 60, 44, 40, 54, 55), 2.0F, 6.9F, null, evolutionsOf(ARBOK), Element.POISON),

    RAICHU(26, "Raichu", baseStats(60, 90, 55, 90, 80, 110), 0.8F, 30.0F, null, evolutionsOf(), Element.ELECTRIC),
    PIKACHU(25, "Pikachu", baseStats(35, 55, 40, 50, 50, 90), 0.4F, 6.0F, null, evolutionsOf(RAICHU), Element.ELECTRIC),

    SANDSLASH(28, "Sandslash", baseStats(75, 100, 110, 45, 55, 65), 1.0F, 29.5F, null, evolutionsOf(), Element.GROUND),
    SANDSHREW(27, "Sandshrew", baseStats(50, 75, 85, 20, 30, 40), 0.6F, 12.0F, null, evolutionsOf(SANDSLASH), Element.GROUND),

    NIDOQUEEN(31, "Nidoqueen", baseStats(90, 92, 87, 75, 85, 76), 1.3F, 60.0F, null, evolutionsOf(), Element.POISON, Element.GROUND),
    NIDORINA(30, "Nidorina", baseStats(70, 62, 67, 55, 55, 56), 0.8F, 20.0F, null, evolutionsOf(NIDOQUEEN), Element.POISON),
    NIDORAN_FEMALE(29, "Nidoran♀", baseStats(55, 47, 52, 40, 40, 41), 0.4F, 7.0F, null, evolutionsOf(NIDORINA), Element.POISON),

    NIDOKING(34, "Nidoking", baseStats(81, 102, 77, 85, 75, 85), 1.4F, 62.0F, null, evolutionsOf(), Element.POISON, Element.GROUND),
    NIDORINO(33, "Nidorino", baseStats(61, 72, 57, 55, 55, 65), 0.9F, 19.5F, null, evolutionsOf(NIDOKING), Element.POISON),
    NIDORAN_MALE(32, "Nidoran♂", baseStats(46, 57, 40, 40, 40, 50), 0.5F, 9.0F, null, evolutionsOf(NIDORINO), Element.POISON),

    CLEFABLE(36, "Clefable", baseStats(95, 70, 73, 95, 90, 60), 1.3F, 40.0F, null, evolutionsOf(), Element.FAIRY),
    CLEFAIRY(35, "Clefairy", baseStats(70, 45, 48, 60, 65, 35), 0.6F, 7.5F, null, evolutionsOf(CLEFABLE), Element.FAIRY),

    NINETALES(38, "Ninetales", baseStats(73, 76, 75, 81, 100, 100), 1.1F, 19.9F, null, evolutionsOf(), Element.FIRE),
    VULPIX(37, "Vulpix", baseStats(38, 41, 40, 50, 65, 65), 0.6F, 9.9F, null, evolutionsOf(NINETALES), Element.FIRE),

    WIGGLYTUFF(40, "Wigglytuff", baseStats(140, 70, 45, 85, 50, 45), 1.0F, 12.0F, null, evolutionsOf(), Element.NORMAL, Element.FAIRY),
    JIGGLYPUFF(39, "Jigglypuff", baseStats(115, 45, 20, 45, 25, 20), 0.5F, 5.5F, null, evolutionsOf(WIGGLYTUFF), Element.NORMAL, Element.FAIRY),

    GOLBAT(42, "Golbat", baseStats(75, 80, 70, 65, 75, 90), 1.6F, 55.0F, null, evolutionsOf(), Element.POISON, Element.FLYING),
    ZUBAT(41, "Zubat", baseStats(40, 45, 35, 30, 40, 55), 0.8F, 7.5F, null, evolutionsOf(GOLBAT), Element.POISON, Element.FLYING),

    VILEPLUME(45, "Vileplume", baseStats(75, 80, 85, 110, 90, 50), 1.2F, 18.6F, null, evolutionsOf(), Element.GRASS, Element.POISON),
    GLOOM(44, "Gloom", baseStats(60, 65, 70, 85, 75, 40), 0.8F, 8.6F, null, evolutionsOf(VILEPLUME), Element.GRASS, Element.POISON),
    ODDISH(43, "Oddish", baseStats(45, 50, 55, 75, 65, 30), 0.5F, 5.4F, null, evolutionsOf(GLOOM), Element.GRASS, Element.POISON),

    PARASECT(47, "Parasect", baseStats(60, 95, 80, 60, 80, 30), 1.0F, 29.5F, null, evolutionsOf(), Element.BUG, Element.GRASS),
    PARAS(46, "Paras", baseStats(35, 70, 55, 45, 55, 25), 0.3F, 5.4F, null, evolutionsOf(PARASECT), Element.BUG, Element.GRASS),

    VENOMOTH(49, "Venomoth", baseStats(70, 65, 60, 90, 75, 90), 1.5F, 12.5F, null, evolutionsOf(), Element.BUG, Element.POISON),
    VENONAT(48, "Venonat", baseStats(60, 55, 50, 40, 55, 45), 1.0F, 30.0F, null, evolutionsOf(VENOMOTH), Element.BUG, Element.POISON),

    DUGTRIO(51, "Dugtrio", baseStats(35, 80, 50, 50, 70, 120), 0.7F, 33.3F, null, evolutionsOf(), Element.GROUND),
    DIGLETT(50, "Diglett", baseStats(10, 55, 25, 35, 45, 95), 0.2F, 0.8F, null, evolutionsOf(DUGTRIO), Element.GROUND),

    PERSIAN(53, "Persian", baseStats(65, 70, 60, 65, 65, 115), 1.0F, 32.0F, null, evolutionsOf(), Element.NORMAL),
    MEOWTH(52, "Meowth", baseStats(40, 45, 35, 40, 40, 90), 0.4F, 4.2F, null, evolutionsOf(PERSIAN), Element.NORMAL),

    GOLDUCK(55, "Golduck", baseStats(80, 82, 78, 95, 80, 85), 1.7F, 76.6F, null, evolutionsOf(), Element.WATER),
    PSYDUCK(54, "Psyduck", baseStats(50, 52, 48, 65, 50, 55), 0.8F, 19.6F, null, evolutionsOf(GOLDUCK), Element.WATER),

    PRIMEAPE(57, "Primeape", baseStats(65, 105, 60, 60, 70, 95), 1.0F, 32.0F, null, evolutionsOf(), Element.FIGHTER),
    MANKEY(56, "Mankey", baseStats(40, 80, 35, 35, 45, 70), 0.5F, 28.0F, null, evolutionsOf(PRIMEAPE), Element.FIGHTER),

    ARCANINE(59, "Arcanine", baseStats(90, 110, 80, 100, 80, 95), 1.9F, 155.0F, null, evolutionsOf(), Element.FIRE),
    GROWLITHE(58, "Growlithe", baseStats(55, 70, 45, 70, 50, 60), 0.7F, 19.0F, null, evolutionsOf(ARCANINE), Element.FIRE),

    POLIWRATH(62, "Poliwrath", baseStats(90, 85, 95, 70, 90, 70), 1.3F, 54.0F, null, evolutionsOf(), Element.WATER, Element.FIGHTER),
    POLIWHIRL(61, "Poliwhirl", baseStats(65, 65, 65, 50, 50, 90), 1.0F, 20.0F, null, evolutionsOf(POLIWRATH), Element.WATER),
    POLIWAG(60, "Poliwag", baseStats(40, 50, 40, 40, 40, 90), 0.6F, 12.4F, null, evolutionsOf(POLIWHIRL), Element.WATER),

    ALAKAZAM(65, "Alakazam", baseStats(55, 50, 45, 135, 95, 120), 1.5F, 48.0F, null, evolutionsOf(), Element.PSYCHIC),
    KADABRA(64, "Kadabra", baseStats(40, 35, 30, 120, 70, 105), 1.3F, 56.5F, null, evolutionsOf(ALAKAZAM), Element.PSYCHIC),
    ABRA(63, "Abra", baseStats(25, 20, 15, 105, 55, 90), 0.9F, 19.5F, null, evolutionsOf(KADABRA), Element.PSYCHIC),

    MACHAMP(68, "Machamp", baseStats(90, 130, 80, 65, 85, 55), 1.6F, 130.0F, null, evolutionsOf(), Element.FIGHTER),
    MACHOKE(67, "Machoke", baseStats(80, 100, 70, 50, 60, 45), 1.5F, 70.5F, null, evolutionsOf(MACHAMP), Element.FIGHTER),
    MACHOP(66, "Machop", baseStats(70, 80, 50, 35, 35, 35), 0.8F, 19.5F, null, evolutionsOf(MACHOKE), Element.FIGHTER),

    VICTREEBEL(71, "Victreebel", baseStats(80, 105, 65, 100, 70, 70), 1.7F, 15.5F, null, evolutionsOf(), Element.GRASS, Element.POISON),
    WEEPINBELL(70, "Weepinbell", baseStats(65, 90, 50, 85, 45, 55), 1.0F, 6.4F, null, evolutionsOf(VICTREEBEL), Element.GRASS, Element.POISON),
    BELLSPROUT(69, "Bellsprout", baseStats(50, 75, 35, 70, 30, 40), 0.7F, 4.0F, null, evolutionsOf(WEEPINBELL), Element.GRASS, Element.POISON),

    TENTACRUEL(73, "Tentacruel", baseStats(80, 70, 65, 80, 120, 100), 1.6F, 55.0F, null, evolutionsOf(), Element.WATER, Element.POISON),
    TENTACOOL(72, "Tentacool", baseStats(40, 40, 35, 50, 100, 70), 0.9F, 45.5F, null, evolutionsOf(TENTACRUEL), Element.WATER, Element.POISON),

    GOLEM(76, "Golem", baseStats(80, 120, 130, 55, 65, 45), 1.4F, 300.0F, null, evolutionsOf(), Element.ROCK, Element.GROUND),
    GRAVELER(75, "Graveler", baseStats(55, 95, 115, 45, 45, 35), 1.0F, 105.0F, null, evolutionsOf(GOLEM), Element.ROCK, Element.GROUND),
    GEODUDE(74, "Geodude", baseStats(40, 80, 100, 30, 30, 20), 0.4F, 20.0F, null, evolutionsOf(GRAVELER), Element.ROCK, Element.GROUND),

    RAPIDASH(78, "Rapidash", baseStats(65, 100, 70, 80, 80, 105), 1.7F, 95.0F, null, evolutionsOf(), Element.FIRE),
    PONYTA(77, "Ponyta", baseStats(50, 85, 55, 65, 65, 90), 1.0F, 30.0F, null, evolutionsOf(RAPIDASH), Element.FIRE),

    SLOWBRO(80, "Slowbro", baseStats(95, 75, 110, 100, 80, 30), 1.6F, 78.5F, null, evolutionsOf(), Element.WATER, Element.PSYCHIC),
    SLOWPOKE(79, "Slowpoke", baseStats(90, 65, 65, 40, 40, 15), 1.2F, 36.0F, null, evolutionsOf(SLOWBRO), Element.WATER, Element.PSYCHIC),

    MAGNETON(82, "Magneton", baseStats(50, 60, 95, 120, 70, 70), 1.0F, 60.0F, null, evolutionsOf(), Element.ELECTRIC, Element.STEEL),
    MAGNEMITE(81, "Magnemite", baseStats(25, 35, 70, 95, 55, 45), 0.3F, 6.0F, null, evolutionsOf(MAGNETON), Element.ELECTRIC, Element.STEEL),

    FARFETCHD(83, "Farfetch'd", baseStats(52, 65, 55, 58, 62, 60), 0.8F, 15.0F, null, evolutionsOf(), Element.NORMAL, Element.FLYING),

    DODRIO(85, "Dodrio", baseStats(60, 110, 70, 60, 60, 110), 1.8F, 85.2F, null, evolutionsOf(), Element.NORMAL, Element.FLYING),
    DODUO(84, "Doduo", baseStats(35, 85, 45, 35, 35, 75), 1.4F, 39.2F, null, evolutionsOf(DODRIO), Element.NORMAL, Element.FLYING),

    DEWGONG(87, "Dewgong", baseStats(90, 70, 80, 70, 95, 70), 1.7F, 120.0F, null, evolutionsOf(), Element.WATER, Element.ICE),
    SEEL(86, "Seel", baseStats(65, 45, 55, 45, 70, 45), 1.1F, 90.0F, null, evolutionsOf(DEWGONG), Element.WATER),

    MUK(89, "Muk", baseStats(105, 105, 75, 65, 100, 50), 1.2F, 30.0F, null, evolutionsOf(), Element.POISON),
    GRIMER(88, "Grimer", baseStats(80, 80, 50, 40, 50, 25), 0.9F, 30.0F, null, evolutionsOf(MUK), Element.POISON),

    CLOYSTER(91, "Cloyster", baseStats(50, 95, 180, 85, 45, 70), 1.5F, 132.5F, null, evolutionsOf(), Element.WATER, Element.ICE),
    SHELLDER(90, "Shellder", baseStats(30, 65, 100, 45, 25, 40), 0.3F, 4.0F, null, evolutionsOf(CLOYSTER), Element.WATER),

    GENGAR(94, "Gengar", baseStats(60, 65, 60, 130, 75, 110), 1.5F, 40.5F, null, evolutionsOf(), Element.GHOST, Element.POISON),
    HAUNTER(93, "Haunter", baseStats(45, 50, 45, 115, 55, 95), 1.6F, 0.1F, null, evolutionsOf(GENGAR), Element.GHOST, Element.POISON),
    GASTLY(92, "Gastly", baseStats(30, 35, 30, 100, 35, 80), 1.3F, 0.1F, null, evolutionsOf(HAUNTER), Element.GHOST, Element.POISON),

    STEELIX(208, "Steelix", baseStats(75, 85, 200, 55, 65, 30), 9.2F, 400.0F, null, evolutionsOf(), Element.STEEL, Element.GROUND),
    ONIX(95, "Onix", baseStats(35, 45, 160, 30, 45, 70), 8.8F, 210.0F, null, evolutionsOf(STEELIX), Element.ROCK, Element.GROUND),

    HYPNO(97, "Hypno", baseStats(85, 73, 70, 73, 115, 67), 1.6F, 75.6F, null, evolutionsOf(), Element.PSYCHIC),
    DROWZEE(96, "Drowzee", baseStats(60, 48, 45, 43, 90, 42), 1.0F, 32.4F, null, evolutionsOf(HYPNO), Element.PSYCHIC),

    KINGLER(99, "Kingler", baseStats(55, 130, 115, 50, 50, 75), 1.3F, 60.0F, null, evolutionsOf(), Element.WATER),
    KRABBY(98, "Krabby", baseStats(30, 105, 90, 25, 25, 50), 0.4F, 6.5F, null, evolutionsOf(KINGLER), Element.WATER),

    ELECTRODE(101, "Electrode", baseStats(60, 50, 70, 80, 80, 150), 1.2F, 66.6F, null, evolutionsOf(), Element.ELECTRIC),
    VOLTORB(100, "Voltorb", baseStats(40, 30, 50, 55, 55, 100), 0.5F, 10.4F, null, evolutionsOf(ELECTRODE), Element.ELECTRIC),

    EXEGGUTOR(103, "Exeggutor", baseStats(95, 95, 85, 125, 75, 55), 2.0F, 120.0F, null, evolutionsOf(), Element.GRASS, Element.PSYCHIC),
    EXEGGCUTE(102, "Exeggcute", baseStats(60, 40, 80, 60, 45, 40), 0.4F, 2.5F, null, evolutionsOf(EXEGGUTOR), Element.GRASS, Element.PSYCHIC),

    MAROWAK(105, "Marowak", baseStats(60, 80, 110, 50, 80, 45), 1.0F, 45.0F, null, evolutionsOf(), Element.GROUND),
    CUBONE(104, "Cubone", baseStats(50, 50, 95, 40, 50, 35), 0.4F, 6.5F, null, evolutionsOf(MAROWAK), Element.GROUND),

    HITMONLEE(106, "Hitmonlee", baseStats(50, 120, 53, 35, 110, 87), 1.5F, 49.8F, null, evolutionsOf(), Element.FIGHTER),
    HITMONCHAN(107, "Hitmonchan", baseStats(50, 105, 79, 35, 110, 76), 1.4F, 50.2F, null, evolutionsOf(), Element.FIGHTER),

    LICKITUNG(108, "Lickitung", baseStats(90, 55, 75, 60, 75, 30), 1.2F, 65.5F, null, evolutionsOf(), Element.NORMAL),

    WEEZING(110, "Weezing", baseStats(65, 90, 120, 85, 70, 60), 1.2F, 9.5F, null, evolutionsOf(), Element.POISON),
    KOFFING(109, "Koffing", baseStats(40, 65, 95, 60, 45, 35), 0.6F, 1.0F, null, evolutionsOf(WEEZING), Element.POISON),

    RHYDON(112, "Rhydon", baseStats(105, 130, 120, 45, 45, 40), 1.9F, 120.0F, null, evolutionsOf(), Element.GROUND, Element.ROCK),
    RHYHORN(111, "Rhyhorn", baseStats(80, 85, 95, 30, 30, 25), 1.0F, 115.0F, null, evolutionsOf(RHYDON), Element.GROUND, Element.ROCK),

    CHANSEY(113, "Chansey", baseStats(250, 5, 5, 35, 105, 50), 1.1F, 34.6F, null, evolutionsOf(), Element.NORMAL),

    TANGELA(114, "Tangela", baseStats(65, 55, 115, 100, 40, 60), 1.0F, 35.0F, null, evolutionsOf(), Element.GRASS),

    KANGASKHAN(115, "Kangaskhan", baseStats(105, 95, 80, 40, 80, 90), 2.2F, 80.0F, null, evolutionsOf(), Element.NORMAL),

    KINGDRA(230, "Kingdra", baseStats(75, 95, 95, 95, 95, 85), 1.8F, 152.0F, null, evolutionsOf(), Element.WATER, Element.DRAGON),
    SEADRA(117, "Seadra", baseStats(55, 65, 95, 95, 45, 85), 1.2F, 25.0F, null, evolutionsOf(KINGDRA), Element.WATER),
    HORSEA(116, "Horsea", baseStats(30, 40, 70, 70, 25, 60), 0.4F, 8.0F, null, evolutionsOf(SEADRA), Element.WATER),

    SEAKING(119, "Seaking", baseStats(80, 92, 65, 65, 80, 68), 1.3F, 39.0F, null, evolutionsOf(), Element.WATER),
    GOLDEEN(118, "Goldeen", baseStats(45, 67, 60, 35, 50, 63), 0.6F, 15.0F, null, evolutionsOf(SEAKING), Element.WATER),

    STARMIE(121, "Starmie", baseStats(60, 75, 85, 100, 85, 115), 1.1F, 80.0F, null, evolutionsOf(), Element.WATER, Element.PSYCHIC),
    STARYU(120, "Staryu", baseStats(30, 45, 55, 70, 55, 85), 0.8F, 34.5F, null, evolutionsOf(STARMIE), Element.WATER),

    MR_MIME(122, "Mr. Mime", baseStats(40, 45, 65, 100, 120, 90), 1.3F, 54.5F, null, evolutionsOf(), Element.PSYCHIC, Element.FAIRY),

    SCIZOR(212, "Scizor", baseStats(70, 130, 100, 55, 80, 65), 1.8F, 118.0F, null, evolutionsOf(), Element.BUG, Element.STEEL),
    SCYTHER(123, "Scyther", baseStats(70, 110, 80, 55, 80, 105), 1.5F, 56.0F, null, evolutionsOf(SCIZOR), Element.BUG, Element.FLYING),

    JYNX(124, "Jynx", baseStats(65, 50, 35, 115, 95, 95), 1.4F, 40.6F, null, evolutionsOf(), Element.ICE, Element.PSYCHIC),

    ELECTIVIRE(466, "Electivire", baseStats(75, 123, 67, 95, 85, 95), 1.8F, 138.6F, null, evolutionsOf(), Element.ELECTRIC),
    ELECTABUZZ(125, "Electabuzz", baseStats(65, 83, 57, 95, 85, 105), 1.1F, 30.0F, null, evolutionsOf(ELECTIVIRE), Element.ELECTRIC),

    MAGMORTAR(467, "Magmortar", baseStats(75, 95, 67, 125, 95, 83), 1.6F, 68.0F, null, evolutionsOf(), Element.FIRE),
    MAGMAR(126, "Magmar", baseStats(65, 95, 57, 100, 85, 93), 1.3F, 44.5F, null, evolutionsOf(MAGMORTAR), Element.FIRE),

    PINSIR(127, "Pinsir", baseStats(65, 125, 100, 55, 70, 85), 1.5F, 55.0F, null, evolutionsOf(), Element.BUG),

    TAUROS(128, "Tauros", baseStats(75, 100, 95, 40, 70, 110), 1.4F, 88.4F, null, evolutionsOf(), Element.NORMAL),

    GYARADOS(130, "Gyarados", baseStats(95, 125, 79, 60, 100, 81), 6.5F, 235.0F, null, evolutionsOf(), Element.WATER, Element.FLYING),
    MAGIKARP(129, "Magikarp", baseStats(20, 10, 55, 15, 20, 80), 0.9F, 10.0F, null, evolutionsOf(GYARADOS), Element.WATER),

    LAPRAS(131, "Lapras", baseStats(130, 85, 80, 85, 95, 60), 2.5F, 220.0F, null, evolutionsOf(), Element.WATER, Element.ICE),

    DITTO(132, "Ditto", baseStats(48, 48, 48, 48, 48, 48), 0.3F, 4.0F, null, evolutionsOf(), Element.NORMAL),

    VAPOREON(134, "Vaporeon", baseStats(130, 65, 60, 110, 95, 65), 1.0F, 29.0F, null, evolutionsOf(), Element.WATER),
    JOLTEON(135, "Jolteon", baseStats(65, 65, 60, 110, 95, 130), 0.8F, 24.5F, null, evolutionsOf(), Element.ELECTRIC),
    FLAREON(136, "Flareon", baseStats(65, 130, 60, 95, 110, 65), 0.9F, 25.0F, null, evolutionsOf(), Element.FIRE),
    ESPEON(196, "Espeon", baseStats(65, 65, 60, 130, 95, 110), 0.9F, 26.5F, null, evolutionsOf(), Element.PSYCHIC),
    UMBREON(197, "Umbreon", baseStats(95, 65, 110, 60, 130, 65), 1.0F, 27.0F, null, evolutionsOf(), Element.DARK),
    LEAFEON(470, "Leafeon", baseStats(65, 110, 130, 60, 65, 95), 1.1F, 25.5F, null, evolutionsOf(), Element.GRASS),
    GLACEON(471, "Glaceon", baseStats(65, 60, 110, 130, 95, 65), 0.8F, 25.9F, null, evolutionsOf(), Element.ICE),
    SYLVEON(700, "Sylveon", baseStats(95, 65, 65, 110, 130, 60), 1.0F, 23.5F, null, evolutionsOf(), Element.FAIRY),
    EEVEE(133, "Eevee", baseStats(55, 55, 50, 45, 65, 55), 0.3F, 6.5F, null, evolutionsOf(VAPOREON, JOLTEON, FLAREON, ESPEON, UMBREON, LEAFEON, GLACEON, SYLVEON), Element.NORMAL),

    PORYGON_Z(474, "Porygon-Z", baseStats(85, 80, 70, 135, 75, 90), 0.9F, 34.0F, null, evolutionsOf(), Element.NORMAL),
    PORYGON2(233, "Porygon2", baseStats(85, 80, 90, 105, 95, 60), 0.6F, 32.5F, null, evolutionsOf(PORYGON_Z), Element.NORMAL),
    PORYGON(137, "Porygon", baseStats(65, 60, 70, 85, 75, 40), 0.8F, 36.5F, null, evolutionsOf(PORYGON2), Element.NORMAL),

    OMASTAR(139, "Omastar", baseStats(70, 60, 125, 115, 70, 55), 1.0F, 35.0F, null, evolutionsOf(), Element.ROCK, Element.WATER),
    OMANYTE(138, "Omanyte", baseStats(35, 40, 100, 90, 55, 35), 0.4F, 7.5F, null, evolutionsOf(OMASTAR), Element.ROCK, Element.WATER),

    KABUTOPS(141, "Kabutops", baseStats(60, 115, 105, 65, 70, 80), 1.3F, 40.5F, null, evolutionsOf(), Element.ROCK, Element.WATER),
    KABUTO(140, "Kabuto", baseStats(30, 80, 90, 55, 45, 55), 0.5F, 11.5F, null, evolutionsOf(KABUTOPS), Element.ROCK, Element.WATER),

    AERODACTYL(142, "Aerodactyl", baseStats(80, 105, 65, 60, 75, 130), 1.8F, 59.0F, null, evolutionsOf(), Element.ROCK, Element.FLYING),

    SNORLAX(143, "Snorlax", baseStats(160, 110, 65, 65, 110, 30), 2.1F, 460.0F, null, evolutionsOf(), Element.NORMAL),

    ARTICUNO(144, "Articuno", baseStats(90, 85, 100, 95, 125, 85), 1.7F, 55.4F, null, evolutionsOf(), Element.ICE, Element.FLYING),
    ZAPDOS(145, "Zapdos", baseStats(90, 90, 85, 125, 90, 100), 1.6F, 52.6F, null, evolutionsOf(), Element.ELECTRIC, Element.FLYING),
    MOLTRES(146, "Moltres", baseStats(90, 100, 90, 125, 85, 90), 2.0F, 60.0F, null, evolutionsOf(), Element.FIRE, Element.FLYING),

    DRAGONITE(149, "Dragonite", baseStats(91, 134, 95, 100, 100, 80), 2.2F, 210.0F, null, evolutionsOf(), Element.DRAGON, Element.FLYING),
    DRAGONAIR(148, "Dragonair", baseStats(61, 84, 65, 70, 70, 70), 4.0F, 16.5F, null, evolutionsOf(DRAGONITE), Element.DRAGON),
    DRATINI(147, "Dratini", baseStats(41, 64, 45, 50, 50, 50), 1.8F, 3.3F, null, evolutionsOf(DRAGONAIR), Element.DRAGON),

    MEWTWO(150, "Mewtwo", baseStats(106, 110, 90, 154, 90, 130), 2.0F, 1220.0F, null, evolutionsOf(), Element.PSYCHIC),

    MEW(151, "Mew", baseStats(100, 100, 100, 100, 100, 100), 0.4F, 4.0F, null, evolutionsOf(), Element.PSYCHIC);

    private final int pokedexId;
    private final String name;
    private final Map<PermanentStat, Integer> baseStats;
    private final float height; // in meters
    private final float weight; // in kg
    private final Function<PermanentStats, Pokemon> maker;
    private final PokemonType[] evolutions;
    private final Element[] elements;

    PokemonType(int pokedexId, String name, Map<PermanentStat, Integer> baseStats, float height, float weight, Function<PermanentStats, Pokemon> maker, PokemonType[] evolutions, Element... elements) {
        this.pokedexId = pokedexId;
        this.name = name;
        this.baseStats = baseStats;
        this.height = height;
        this.weight = weight;
        this.maker = maker;
        this.evolutions = evolutions;
        this.elements = elements;
    }

    private static Map<PermanentStat, Integer> baseStats(int hp, int atk, int def, int spAtk, int spDef, int speed) {
        Map<PermanentStat, Integer> stats = new HashMap<>();
        stats.put(PermanentStat.HP, hp);
        stats.put(PermanentStat.ATK, atk);
        stats.put(PermanentStat.DEF, def);
        stats.put(PermanentStat.SP_ATK, spAtk);
        stats.put(PermanentStat.SP_DEF, spDef);
        stats.put(PermanentStat.SPEED, speed);
        return stats;
    }

    private static PokemonType[] evolutionsOf(PokemonType... types) {
        return types;
    }
}
