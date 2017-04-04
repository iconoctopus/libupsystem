/*
 * Copyright (C) 2017 ykonoclast
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.duckdns.spacedock.upengine.libupsystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import org.duckdns.spacedock.commonutils.ErrorHandler;
import org.duckdns.spacedock.commonutils.PropertiesHandler;
import org.duckdns.spacedock.upengine.libupsystem.RollUtils.RollResult;

public class Perso
{

    /**
     * l'indice de l'action courantes dans le tableau des actions
     */
    private int m_actionCourante;

    /**
     * les actions du personnage dans ce tour sous la forme de la phase dans
     * laquelle l'action se déroule dans l'ordre des phases (donc tableau de la
     * taille de l'init) ,celles consommées sont fixées à 11
     */
    private ArrayList<Integer> m_actions;
    /**
     * arbre des domaines/compétences du personnage
     */
    private final ArbreDomaines m_arbreDomaines;
    /**
     * rassemble armes et armures du personnage ainsi que quelques
     * fonctionalités utiles notamment les armes et armures courantes
     */
    private final Inventaire m_inventaire = new Inventaire();
    /**
     * non finale pour augmenter à l'xp et définition hors constructeur
     */
    private CoupleJauge m_jaugeFatigueForceDAme;
    /**
     * non finale pour augmenter à l'xp et définition hors constructeur
     */
    private CoupleJauge m_jaugeSanteInit;
    /**
     * le nom du personage
     */
    private String m_libellePerso;
    /**
     * les traits du personnage
     */
    private final EnumMap<Trait, Integer> m_traits;

    /**
     * Constructeur de Perso prenant des caractéristiques en paramétres. Il est
     * possible de le modifier par la suite on peut l'initialiser avec presque
     * rien, c'est le constructeur du cas général
     *
     * @param p_traits
     * @param p_arbre
     */
    public Perso(EnumMap<Trait, Integer> p_traits, ArbreDomaines p_arbre)
    {
	m_libellePerso = "Perso";

	for (Integer i : p_traits.values())
	{
	    if (i < 0)
	    {
		ErrorHandler.paramAberrant(PropertiesHandler.getInstance("libupsystem").getString("trait") + ":" + i);
	    }
	}
	m_traits = p_traits;
	m_arbreDomaines = p_arbre;

	//configuration automatique des autres caractéristiques maintenant possible car les traits sont connus
	initPerso();
    }

    /**
     * constructeur produisant des PNJ générés par rang de menace (RM)
     *
     * @param p_RM
     */
    public Perso(int p_RM)
    {
	if (p_RM < 1)
	{
	    ErrorHandler.paramAberrant(PropertiesHandler.getInstance("libupsystem").getString("rang") + ":" + p_RM);
	}
	//configuration des traits
	m_traits = new EnumMap<Trait, Integer>(Trait.class);
	m_traits.put(Trait.PHYSIQUE, p_RM);
	m_traits.put(Trait.COORDINATION, p_RM);
	m_traits.put(Trait.MENTAL, p_RM - 1);
	m_traits.put(Trait.VOLONTE, p_RM - 1);
	m_traits.put(Trait.PRESENCE, p_RM - 1);

	//configuration automatique des autres caractéristiques maintenant possible car les traits sont connus
	initPerso();

	//configuration des caractéristiques de combat une fois que l'arbre des domaines est généré
	//configuration du domaine corps à corps
	m_arbreDomaines = new ArbreDomaines();
	m_arbreDomaines.setRangDomaine(3, p_RM);
	for (int i = 0; i < UPReferenceSysteme.getInstance().getListComp(3).size(); i++)
	{
	    m_arbreDomaines.setRangComp(3, i, p_RM);
	}

	//idem pour tout le domaine combat à distance
	m_arbreDomaines.setRangDomaine(4, p_RM);
	for (int i = 0; i < UPReferenceSysteme.getInstance().getListComp(4).size(); i++)
	{
	    m_arbreDomaines.setRangComp(4, i, p_RM);
	}

	//on ajoute des rangs en esquive
	m_arbreDomaines.setRangDomaine(2, p_RM);
	m_arbreDomaines.setRangComp(2, 0, p_RM);

	m_libellePerso = PropertiesHandler.getInstance("libupsystem").getString("lbl_perso_std") + p_RM;
    }

    /**
     *
     * @param p_domaine
     * @param p_comp
     * @param p_specialite
     */
    public void addSpecialite(int p_domaine, int p_comp, String p_specialite)
    {
	m_arbreDomaines.addSpecialite(p_domaine, p_comp, p_specialite);
    }

    /**
     * fait dépenser une action au personnage dans la phase en cours si c'est
     * possible
     *
     * @param p_phaseActuelle
     * @return un booléen indiquant si il est possible d'agir dans la phase en
     * cours
     */
    public boolean agirEnCombat(int p_phaseActuelle)
    {
	boolean result = false;
	if (p_phaseActuelle > 0 && p_phaseActuelle < 11)
	{
	    if (isActif(p_phaseActuelle))
	    {
		m_actions.set(m_actionCourante, 11);
		m_actionCourante++;
		result = true;
	    }
	}
	else
	{
	    ErrorHandler.paramAberrant(PropertiesHandler.getInstance("libupsystem").getString("phase") + ":" + p_phaseActuelle);
	}
	return result;
    }

    /**
     * Fait effectuer une attaque au corps à corps au personnage. Le personnage
     * attaquera systématiquement avec l'arme courante, il faut donc la
     * configurer avant. Cette méthode vérifie que l'action est possible dans la
     * phase courante en fonction de l'init du perso, elle est donc conçue pour
     * le combat uniquement. Si l'on veut utiliser une autre arme il faut
     * d'abord la configurer comme arme courante. Une exception est levée si
     * l'on essaye d'attaquer en corps à corps avec une arme à distance (rien
     * n'interdit d'entrer une distance 0 dans l'autre méthode adaptée). Si
     * aucune arme n'est équipée on utilise par défaut les mains nues.
     *
     * Il est important de garder la génération des dégâts séparée et déclenchée
     * depuis l'extérieur afin que le contrôleur puisse choisir d'utiliser les
     * incréments pour autre chose que des dégâs (ciblage, autoriser parade...).
     *
     * @param p_phaseActuelle
     * @param p_ND
     * @return
     */
    public final RollUtils.RollResult attaquerCaC(int p_phaseActuelle, int p_ND)
    {
	ArmeCaC arme = (ArmeCaC) m_inventaire.getArmeCourante();
	int catArm = 0;//mains nues par défaut
	if (arme != null)//une arme est équipée
	{
	    catArm = arme.getCategorie();
	}
	return effectuerAttaque(p_phaseActuelle, p_ND, catArm * 2, 3, 0, 0, 0);//par convention les comp d'attaque de CaC sont à cat*2, les parades sont à Cat*2+1
    }

    /**
     * Fait effectuer une attaque à distance au personnage. Cette méthode
     * vérifie que l'action est possible dans la phase courante en fonction de
     * l'init du perso, elle est donc conçue pour le combat uniquement. On
     * utilise l'arme courante ou les mains nues. Si l'on veut utiliser une
     * autre arme il faut d'abord la configurer comme arme courante. Une
     * exception est levée si l'on essaye d'attaquer à distance avec une arme de
     * corps à corps (rien n'interdit d'entrer une distance 0 dans l'autre
     * méthode adaptée). Aucune vérification n'est effectuée sur le magasin
     * actuel de l'arme. Si celui-ci n'est pas suffisant l'arme lèvera une
     * exception.
     *
     * Il est important de garder la génération des dégâts séparée et déclenchée
     * depuis l'extérieur afin que le contrôleur puisse choisir d'utiliser les
     * incréments pour autre chose que des dégâs (ciblage, autoriser parade...).
     *
     * @param p_phaseActuelle
     * @param p_ND
     * @param p_distance la distance de la cible
     * @param p_nbCoups nombre de tirs effectués (pour la règle
     * @return
     */
    public RollResult attaquerDist(int p_phaseActuelle, int p_ND, int p_distance, int p_nbCoups)
    {//TODO déplacer l'esentiel de ce code dans ArmeDist (ce n'est pas au perso d'embarquer la logique de traitement du combat a distance ou de veiller à consommer les muns) via une méthode renvoyant un objet de com dédié avec les modificateurs afférents
	RollResult result = new RollResult(0, false, 0);//raté par défaut
	ArmeDist arme = (ArmeDist) m_inventaire.getArmeCourante();
	int modDist = 0;
	if (p_distance >= 0 && p_nbCoups > 0 && p_nbCoups <= 20)
	{
	    arme.consommerMun(p_nbCoups);//on consomme les coups, une exception sera levée si il n'y a pas assez de munitions, le code appelant devrait vérifier systématiquement cela
	    if (p_distance <= arme.getPortee())//échec auto si distance > portée
	    {
		if (p_distance <= (int) Math.round((double) arme.getPortee() / (double) 2))
		{//portée courte
		    modDist -= arme.getMalusCourt();
		}
		else
		{//portée longue
		    modDist -= arme.getMalusLong();
		}
		//tir effectif, maintenant il faut calculer l'éventuel bonus de rafale
		int bonusDesLancesRafale = 0;
		int bonusDesGardesRafale = 0;

		if (p_nbCoups > 1)
		{
		    if (arme.getCategorie() == 4)//rafales acceptées, sinon lever une exception
		    {
			if (p_nbCoups >= 3)//les bonus commmencent à partir de 3 balles
			{
			    if (p_nbCoups < 4)//rafale courte
			    {
				bonusDesLancesRafale = 2;
			    }
			    else
			    {
				if (p_nbCoups < 10)//rafale moyenne
				{
				    int preResult = (p_nbCoups / 3);//division entre int donc troncature
				    bonusDesLancesRafale = preResult * 2;
				}
				else//rafale longue
				{
				    bonusDesLancesRafale = bonusDesGardesRafale = (p_nbCoups / 5);//division entre int donc troncature
				}
			    }
			}
		    }
		    else
		    {
			ErrorHandler.paramAberrant(PropertiesHandler.getInstance("libupsystem").getString("nbCoups") + ":" + p_nbCoups);
		    }

		}
		result = effectuerAttaque(p_phaseActuelle, p_ND, arme.getCategorie(), 4, bonusDesLancesRafale, bonusDesGardesRafale, modDist);
	    }
	}
	else
	{
	    ErrorHandler.paramAberrant(PropertiesHandler.getInstance("libupsystem").getString("distance") + ":" + p_distance + " " + PropertiesHandler.getInstance("libupsystem").getString("nbCoups") + ":" + p_nbCoups);
	}
	return result;
    }

    /**
     * fait effectuer au personnage un jet de l'une de ses compétences. Appelé
     * en interne par les méthodes d'attaque qui effectuent les pré-traitements
     * pour aboutir aux caractéristiques finales du jet.
     *
     * @param p_trait id du trait à utiliser (pas la valeur!)
     * @param p_ND
     * @param p_comp
     * @param p_domaine
     * @param p_modifNbLances
     * @param p_modifNbGardes
     * @param p_modifScore
     * @return le résultat du jet
     */
    public final RollUtils.RollResult effectuerJetComp(Trait p_trait, int p_domaine, int p_comp, int p_ND, int p_modifNbLances, int p_modifNbGardes, int p_modifScore)
    {
	return m_arbreDomaines.effectuerJetComp(m_traits.get(p_trait), p_domaine, p_comp, p_ND, p_modifNbLances, p_modifNbGardes, p_modifScore, isSonne());
    }

    /**
     * fait effectuer au personnage un jet avec l'un de ses traits purs.
     *
     * @param p_trait
     * @param p_ND
     * @return le résultat du jet
     */
    public final RollUtils.RollResult effectuerJetTrait(Trait p_trait, int p_ND)
    {
	return RollUtils.extraireIncrements(RollUtils.lancer(m_traits.get(p_trait), m_traits.get(p_trait), isSonne()), p_ND);
    }

    /**
     * inflige des dégâts à ce perso, via la jauge de Santé après avoir appliqué
     * les effets d'armure
     *
     * @param p_degats
     */
    public void etreBlesse(Degats p_degats)
    {
	if (p_degats.getQuantite() >= 0 && p_degats.getTypeArme() >= 0)
	{
	    Armure armure = m_inventaire.getArmure();
	    int redDegats = armure.getRedDegats(p_degats.getTypeArme());
	    int degatsEffectifs = p_degats.getQuantite() - redDegats;

	    if (degatsEffectifs > 0)
	    {
		m_jaugeSanteInit.recevoirDegats(degatsEffectifs, this);
	    }
	}
	else
	{
	    ErrorHandler.paramAberrant(PropertiesHandler.getInstance("libupsystem").getString("degats") + ":" + p_degats.getQuantite() + " " + PropertiesHandler.getInstance("libupsystem").getString("type") + ":" + p_degats.getTypeArme());
	}
    }

    /**
     * génère l'initiative du personnage, devrait être appelée dans le
     * constructeur mais par la suite contrôlée de l'extérieur
     */
    public final void genInit()
    {
	int initiative = m_jaugeSanteInit.getRemplissage_externe();
	m_actionCourante = 0;
	ArrayList<Integer> tabResult = new ArrayList<>();
	if (initiative > 0)
	{
	    for (int i = 0; i < initiative; i++)
	    {
		tabResult.add(RollUtils.lancer(1, 1, true));
	    }
	}
	Collections.sort(tabResult);
	m_actions = tabResult;
    }

    /**
     * génère des dégâts avec l'arme courante (distance ou corps à corps),
     * séparée de l'attaque pour que le contrôleur puisse utiliser les
     * incréments pour autre chose (comme cibler ou permettre une défense)
     *
     * @param p_increments
     * @return
     */
    public Degats genererDegats(int p_increments)
    {
	int domaine;
	int competence;
	int vd;
	int bonusSup = p_increments * 2;//bonus aux dégâts du aux incréments (pas de ciblage dans cette méthode)
	int typArm;

	Degats result = new Degats(0, 0);

	if (p_increments >= 0)
	{
	    Arme arme = m_inventaire.getArmeCourante();

	    if (arme != null)//armes équipées
	    {
		vd = arme.getVD();
		typArm = arme.getTypeArme();

		if (arme.getMode() == 0)//arme de corps à corps employée
		{
		    domaine = 3;//corps à corps
		    competence = arme.getCategorie() * 2;//les attaques sont à catégorie *2, les parades à catégorie * 2 +1
		    bonusSup += m_traits.get(Trait.PHYSIQUE);//le rang de physique vient en bonus au nombre d'incréments
		}
		else//arme à distance employée
		{
		    domaine = 4;//distance
		    competence = arme.getCategorie();//compétence d'arme
		    //pas de bonus de physique pour les armes à distance
		}
	    }
	    else//combat à mains nues
	    {
		vd = m_traits.get(Trait.PHYSIQUE);
		typArm = 0;
		domaine = 3;//corps à corps
		competence = 0;//comp mains nues
		bonusSup += m_traits.get(Trait.PHYSIQUE);//le rang de physique vient en bonus au nombre d'incréments
	    }
	    return new Degats(vd + m_arbreDomaines.getRangDomaine(domaine) + m_arbreDomaines.getRangComp(domaine, competence) + bonusSup, typArm);
	}
	else
	{
	    ErrorHandler.paramAberrant(PropertiesHandler.getInstance("libupsystem").getString("increments") + ":" + p_increments);
	}
	return result;
    }

    /**
     *
     * @return une copie : la liste n'est pas modifiable de l'extérieur
     */
    public ArrayList<Integer> getActions()
    {
	return new ArrayList<>(m_actions);
    }

    public int getBlessuresGraves()
    {
	return m_jaugeSanteInit.getRemplissage_interne();
    }

    public int getBlessuresLegeres()
    {
	return m_jaugeSanteInit.getBlessuresLegeres();
    }

    public int getBlessuresLegeresMentales()
    {
	return m_jaugeFatigueForceDAme.getBlessuresLegeres();
    }

    /**
     *
     * @return l'initiative totale du personnage en comptant le bonus de l'arme
     */
    public int getInitTotale()
    {
	int result = 0;

	//traitement de la partie dûe aux dés d'action
	for (int i = 0; i < m_actions.size(); ++i)
	{
	    if (m_actions.get(i) < 11)//si l'action considérée est toujours disponible
	    {
		result += m_actions.get(i);
	    }
	}

	//traitement du bonus dû à l'arme
	Arme arme = m_inventaire.getArmeCourante();
	if (arme != null)
	{
	    result += arme.getBonusInit() * 5;
	}
	return result;
    }

    public Inventaire getInventaire()
    {
	return m_inventaire;
    }

    /**
     *
     * @param libellePerso
     */
    public void setLibellePerso(String libellePerso)
    {
	this.m_libellePerso = libellePerso;
    }

    /**
     *
     * @param p_typeArme
     * @param p_catArme catégorie d'arme à employer en parade, ignoré si
     * esquive, attnetion ce n'est pas le numéro index de comp mais bien la
     * catégorie d'arme
     * @param p_esquive : si l'esquive doit être employée, sinon c'est une
     * parade qui est effectuée
     * @return le ND passif calculé à partir des comps et de l'armure
     */
    public int getNDPassif(int p_typeArme, int p_catArme, boolean p_esquive)
    {
	int ND;
	int rang;
	int effetArmure = 0;
	Armure armure = m_inventaire.getArmure();

	if (!p_esquive)
	{
	    //calcul de la valeur issue de la compétence parade
	    rang = m_arbreDomaines.getRangComp(3, p_catArme * 2 + 1); // la comp de parade est par convention à cat*2+1 là où attaque est à cat*2

	    //ajout des bonus  et malus d'armure
	    effetArmure += armure.getBonusND(p_typeArme);
	    effetArmure -= armure.getMalusParade();
	}
	else
	{
	    //calcul de la valeur issue de la compétence esquive
	    rang = m_arbreDomaines.getRangComp(2, 0);
	    //ajout des bonus  et malus d'armure
	    effetArmure += armure.getBonusND(p_typeArme);
	    effetArmure -= armure.getMalusEsquive();

	}

	if (rang > 0)
	{
	    ND = rang * 5 + 5;
	    if (rang >= 3)
	    {
		ND += 5;
	    }
	}
	else
	{
	    if (!p_esquive)
	    {
		rang = m_arbreDomaines.getRangDomaine(3);
	    }
	    else
	    {
		rang = m_arbreDomaines.getRangDomaine(2);
	    }
	    ND = rang * 5 - 5;
	    if (ND < 5)
	    {
		ND = 5;
	    }
	}
	ND += effetArmure;
	return ND;
    }

    public int getPointsDeFatigue()
    {
	return m_jaugeFatigueForceDAme.getRemplissage_interne();
    }

    /**
     *
     * @param p_domaine
     * @param p_comp
     * @return
     */
    public int getRangComp(int p_domaine, int p_comp)
    {
	return m_arbreDomaines.getRangComp(p_domaine, p_comp);
    }

    /**
     *
     * @param p_domaine
     * @return
     */
    public int getRangDomaine(int p_domaine)
    {
	return m_arbreDomaines.getRangDomaine(p_domaine);
    }

    /**
     *
     * @param p_domaine
     * @param p_comp
     * @return
     */
    public ArrayList<String> getSpecialites(int p_domaine, int p_comp)
    {
	return m_arbreDomaines.getSpecialites(p_domaine, p_comp);
    }

    /**
     *
     * @param p_trait le trait considéré
     * @return la valeur du trait
     */
    public int getTrait(Trait p_trait)
    {
	return m_traits.get(p_trait);
    }

    /**
     * renvoie vrai si le personnage a une action dans la phase active
     * comportement indéfini si demande pour pĥase ultérieure ou antérieure
     *
     * @param p_phaseActuelle
     * @return
     */
    public boolean isActif(int p_phaseActuelle)
    {
	if (p_phaseActuelle <= 0 || p_phaseActuelle > 10)
	{
	    ErrorHandler.paramAberrant(PropertiesHandler.getInstance("libupsystem").getString("phase") + ":" + p_phaseActuelle);
	}
	return ((m_actions.size() - m_actionCourante) > 0 && p_phaseActuelle == m_actions.get(m_actionCourante));//si l'indice dans le tableau des actions indique que toutes celles-ci n'ont pas été consommées et si l'action pointée par cet indice correspond à la phase actuelle
    }

    /**
     * les 2 jauges sont prises en compte
     *
     * @return
     */
    public boolean isElimine()
    {
	return (m_jaugeFatigueForceDAme.isElimine() || m_jaugeSanteInit.isElimine());
    }

    /**
     * les 2 jauges sont prises en compte
     *
     * @return
     */
    public boolean isInconscient()
    {
	return m_jaugeSanteInit.isInconscient() || m_jaugeFatigueForceDAme.isInconscient();
    }

    /**
     * les 2 jauges sont prises en compte
     *
     * @return
     */
    public boolean isSonne()
    {
	return (m_jaugeSanteInit.isSonne() || m_jaugeFatigueForceDAme.isSonne());
    }

    /**
     *
     * @param p_domaine
     * @param p_comp
     * @param p_indiceSpe
     */
    public void removeSpecialite(int p_domaine, int p_comp, int p_indiceSpe)
    {
	m_arbreDomaines.removeSpecialite(p_domaine, p_comp, p_indiceSpe);
    }

    /**
     *
     * @param p_domaine
     * @param p_comp
     * @param p_rang
     */
    public void setRangComp(int p_domaine, int p_comp, int p_rang)
    {
	m_arbreDomaines.setRangComp(p_domaine, p_comp, p_rang);
    }

    /**
     *
     * @param p_domaine
     * @param p_rang
     */
    public void setRangDomaine(int p_domaine, int p_rang)
    {
	m_arbreDomaines.setRangDomaine(p_domaine, p_rang);
    }

    /**
     *
     * @param p_trait le trait considéré
     * @param p_valeur
     */
    public void setTrait(Trait p_trait, int p_valeur)
    {
	m_traits.put(p_trait, p_valeur);
	initJauges();//TODO : en l'état les jauges sont complètement remplacées : on perd donc les blessures, la force d'âme dépensée etc.
    }

    @Override
    public String toString()
    {
	return m_libellePerso;
    }

    /**
     * Méthode où les éléments communs d'attaque se déroulent : les méthodes
     * précédentes ont calculé les bonus/malus et diverses conditions de
     * l'attaque spécifiques à leur situation (distance ou CaC), celle-ci va
     * prendre en compte tous les éléments communs et aire exécuter le jet à la
     * méthode afférente
     *
     * @param p_phaseActuelle
     * @param p_ND
     * @param p_comp
     * @param p_domaine
     * @param p_modifNbLances
     * @param p_modifNbGardes
     * @param p_modifScore
     * @return
     */
    private RollResult effectuerAttaque(int p_phaseActuelle, int p_ND, int p_comp, int p_domaine, int p_modifNbLances, int p_modifNbGardes, int p_modifScore)
    {
	RollResult result = null;
	if (agirEnCombat(p_phaseActuelle))
	{
	    int modDesLances = 0 + p_modifNbLances;
	    int modDesGardes = 0 + p_modifNbGardes;
	    int modFinal = 0 + p_modifScore;
	    int ecartPhyMin = 0;

	    Arme arme = m_inventaire.getArmeCourante();

	    if (p_comp != 0)//on utilise une arme, il faut prendre en compte ses éventuels malus
	    {
		{
		    if (arme.getphysMin() > m_traits.get(Trait.PHYSIQUE))
		    {
			ecartPhyMin += m_traits.get(Trait.PHYSIQUE) - arme.getphysMin();
		    }
		}
		modDesLances -= arme.getMalusAttaque();
	    }
	    modFinal += (ecartPhyMin * 10);
	    result = effectuerJetComp(Trait.COORDINATION, p_domaine, p_comp, p_ND, modDesLances, modDesGardes, modFinal);
	}
	return result;
    }

    /**
     * initialise les jauges du personnage avec le tableau de ses traits, doit
     * donc être appelé par le onstructeur après cette initialisation
     */
    private void initJauges()
    {
	int traitMin = m_traits.get(Trait.PHYSIQUE);

	for (Integer i : m_traits.values())
	{
	    if (i < traitMin)
	    {
		traitMin = i;
	    }
	}
	m_jaugeFatigueForceDAme = new CoupleJauge(m_traits.get(Trait.PHYSIQUE), m_traits.get(Trait.VOLONTE), traitMin);
	m_jaugeSanteInit = new CoupleJauge(m_traits.get(Trait.PHYSIQUE), m_traits.get(Trait.VOLONTE), m_traits.get(Trait.MENTAL), m_traits.get(Trait.COORDINATION));
    }

    /**
     * initialise les caractéristiques hors traits du personnage
     */
    private void initPerso()
    {
	initJauges();
	genInit();
    }

    //enum contenant les différents traits possibles
    public enum Trait
    {
	PHYSIQUE, COORDINATION, VOLONTE, MENTAL, PRESENCE
    }

    /**
     * classe utilisée pour encapsuler les résultats d'une attaque réussie ; des
     * dégâts mais aussi le type. On n'utilise pas de collections clé/valeur
     * comme une EnumMap car l'on veut juste un accès simple à des champs
     * définis : inutile de dégrader les performances avec toute la mécanique
     * des collections.
     */
    public static final class Degats
    {

	/**
	 * le total des dégâts infligés
	 */
	private int m_quantite;
	/**
	 * le type d'arme employé
	 */
	private int m_typeArme;

	/**
	 * constructeur de dégâts
	 *
	 * @param p_quantite
	 * @param p_typeArme
	 */
	public Degats(int p_quantite, int p_typeArme)
	{
	    if (p_quantite >= 0 && p_typeArme >= 0)
	    {
		m_quantite = p_quantite;
		m_typeArme = p_typeArme;
	    }
	    else
	    {
		ErrorHandler.paramAberrant(PropertiesHandler.getInstance("libupsystem").getString("degats") + ":" + p_quantite + " " + PropertiesHandler.getInstance("libupsystem").getString("type") + ":" + p_typeArme);
	    }
	}

	/**
	 * @return the m_quantite
	 */
	public int getQuantite()
	{
	    return m_quantite;
	}

	/**
	 * @return the m_typeArme
	 */
	public int getTypeArme()
	{
	    return m_typeArme;
	}
    }
}
