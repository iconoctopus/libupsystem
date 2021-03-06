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
import javax.json.JsonArray;
import javax.json.JsonObject;
import static org.duckdns.spacedock.commonutils.JSONHandler.loadJsonFile;

/**
 * classe peremttant l'accès aux éléments de référence concernant les armures
 *
 * @author ykonoclast
 */
public class UPReferenceArmures
{

    /**
     * list des libellés des matériaux d'armures
     */
    private final JsonArray m_listLblMatArmures;
    /**
     * list des libellés des matériaux d'armures
     */
    private final JsonArray m_listLblMatArmuresAnciennes;
    /**
     * liste des libellés des localisations
     */
    private final JsonArray m_listLblLoca;
    /**
     * liste de libellés des types d'armures
     */
    private final JsonArray m_listLblTypArmures;
    /**
     * table de tous les boucliers
     */
    private final JsonArray m_tabBoucliers;
    /**
     * tableau des pièces d'armure
     */
    private final JsonArray m_tabPiecesArmures;
    /**
     * table des ajustemnts à effectuer sur les points d'armure pour adapter un
     * type d'armure à un type d'arme. C'est un tableau de tableaux (un pour
     * chaque type d'armure), chaque tableau interne est indicé par les types
     * d'armes
     */
    private final JsonArray m_tableAjustementArmure;
    /**
     * table des bonus au ND, indexée par le rang d'armure (issu du tableau des
     * rangs)
     */
    private final JsonArray m_tableArmureBonusND;
    /**
     * table des malus d'armure indexée par le rang d'armure (issu du tableau
     * des rangs)
     */
    private final JsonArray m_tableArmureMalus;
    /**
     * table des rangs d'armure, à chaque rang (indice) correspond un seuil de
     * points d'armure à atteindre
     */
    private final JsonArray m_tableArmureRangs;
    /**
     * table des réductions de dégâts, indexée par le rang d'armure (issu du
     * tableau des rangs)
     */
    private final JsonArray m_tableArmureRedDegats;
    /**
     * instance unique de cet objet
     */
    private static UPReferenceArmures m_instance;

    /**
     * véritable constructeur privé effectuant tous les accès fichiers à
     * l'instanciation afin de limiter les temps de latence à une grosse fois
     */
    private UPReferenceArmures()
    {
	JsonObject object;

	object = loadJsonFile("libupsystem", "equipement/caracs_armures.json");
	m_tableArmureBonusND = object.getJsonArray("bonusND");
	m_tableArmureRedDegats = object.getJsonArray("red_degats");
	m_tableArmureMalus = object.getJsonArray("malus_armure");
	m_tableArmureRangs = object.getJsonArray("rangs");
	m_tableAjustementArmure = object.getJsonArray("ajustements");
	m_tabPiecesArmures = object.getJsonArray("pieces");
	m_listLblMatArmures = object.getJsonArray("materiaux_armures");
	m_listLblMatArmuresAnciennes = object.getJsonArray("materiaux_armures_anciennes");
	m_listLblTypArmures = object.getJsonArray("types_armures");
	m_tabBoucliers = object.getJsonArray("boucliers");
	m_listLblLoca = object.getJsonArray("localisations");
    }

    /**
     *
     * @return l'instance unique de la référence, la construit si elle n'existe
     * pas
     */
    public static UPReferenceArmures getInstance()
    {
	if (m_instance == null)
	{
	    m_instance = new UPReferenceArmures();
	}
	return (m_instance);
    }

    /**
     *
     * @param p_points les points d'armure
     * @param p_typeArme
     * @param p_typeArmure
     * @return le bonus au ND offert par l'armure sur ce type d'arme
     */
    public int getBonusND(int p_points, int p_typeArme, int p_typeArmure)
    {
	int pointsEffectifs = getPtsArmureEffectifs(p_points, p_typeArme, p_typeArmure);

	int resultat = 0;

	int rang = getRang(pointsEffectifs);
	if (rang >= 0)//impossible autrement si l'on est arrivé jusque là mais on ne sait jamais
	{
	    resultat = m_tableArmureBonusND.getInt(rang);
	}
	return resultat;
    }

    /**
     *
     * @param p_points
     * @param p_typeArme
     * @param p_typeArmure
     * @return la réduction aux dégâts de l'armure par rapport au type d'arme
     */
    public int getRedDegats(int p_points, int p_typeArme, int p_typeArmure)
    {
	int pointsEffectifs = getPtsArmureEffectifs(p_points, p_typeArme, p_typeArmure);

	int resultat = 0;

	int rang = getRang(pointsEffectifs);
	if (rang >= 0)//impossible autrment si l'on est arrivé jusque là mais on ne sait jamais
	{
	    resultat = m_tableArmureRedDegats.getInt(rang);
	}
	return resultat;
    }

    /**
     *
     * @param p_nbPoints
     * @return la réduction aux dégâts de l'armure par rapport au type d'arme
     */
    public int getMalusArmure(int p_nbPoints)
    {
	int resultat = 0;

	int rang = getRang(p_nbPoints);
	if (rang >= 0)//impossible autrment si l'on est arrivé jusque là mais on ne sait jamais
	{
	    resultat = m_tableArmureMalus.getInt(rang);
	}
	return resultat;
    }

    /**
     *
     * @param p_indice
     * @return le libllé d'une localisation
     */
    public String getLblLoca(int p_indice)
    {
	return m_listLblLoca.getString(p_indice);
    }

    /**
     *
     * @param p_indice
     * @return le libellé d'un matériau d'armure
     */
    public String getLblMateriauArmure(int p_indice)
    {
	return m_listLblMatArmures.getString(p_indice);
    }

    /**
     *
     * @param p_indice
     * @return le libellé d'un matériau d'armure ancienne
     */
    public String getLblMateriauArmureAncienne(int p_indice)
    {
	return m_listLblMatArmuresAnciennes.getString(p_indice);
    }

    /**
     *
     * @param p_indice
     * @return le libellé d'une pièce d'armure
     */
    public String getLblPiece(int p_indice)
    {
	JsonObject piece = m_tabPiecesArmures.getJsonObject(p_indice);
	return piece.getString("lbl");
    }

    /**
     *
     * @param p_indice
     * @return le libellé d'un bouclier
     */
    public String getLblBouclier(int p_indice)
    {
	JsonObject objetIntermediaire = m_tabBoucliers.getJsonObject(p_indice);
	return objetIntermediaire.getString("lbl");
    }

    /**
     *
     * @param p_indice
     * @return le libllé d'un type d'armure
     */
    public String getLblTypeArmure(int p_indice)
    {
	return m_listLblTypArmures.getString(p_indice);
    }

    /**
     * la liste des noms de tous les boucliers
     *
     * @return
     */
    public ArrayList<String> getListBouclier()
    {
	ArrayList<String> res = new ArrayList<>();
	for (int i = 0; i < m_tabBoucliers.size(); ++i)
	{
	    res.add(m_tabBoucliers.getJsonObject(i).getString("lbl"));
	}
	return res;
    }

    /**
     * la liste des noms de tous les matériaux d'armure
     *
     * @return
     */
    public ArrayList<String> getListMateriauArmure()
    {
	ArrayList<String> res = new ArrayList<>();
	for (int i = 0; i < m_listLblMatArmures.size(); ++i)
	{
	    res.add(m_listLblMatArmures.getString(i));
	}
	return res;
    }

    /**
     * la liste des noms de toutes les pieces d'armures
     *
     * @return
     */
    public ArrayList<String> getListPieceArmure()
    {
	ArrayList<String> res = new ArrayList<>();
	for (int i = 0; i < m_tabPiecesArmures.size(); ++i)
	{
	    res.add(m_tabPiecesArmures.getJsonObject(i).getString("lbl"));
	}
	return res;
    }

    /**
     * la liste des noms de tous les types d'armure
     *
     * @return
     */
    public ArrayList<String> getListTypeArmure()
    {
	ArrayList<String> res = new ArrayList<>();
	for (int i = 0; i < m_listLblTypArmures.size(); ++i)
	{
	    res.add(m_listLblTypArmures.getString(i));
	}
	return res;
    }

    /**
     *
     * @param p_indice
     * @return la localisation d'une pièce
     */
    public int getLocalisation(int p_indice)
    {
	JsonObject piece = m_tabPiecesArmures.getJsonObject(p_indice);
	return piece.getInt("loca");
    }

    /**
     *
     * @param p_idPiece
     * @param p_materiau
     * @return le nombre de points d'armure d'une pièce d'un matériau spécifiés
     * par leurs indices
     */
    public int getPtsPiece(int p_idPiece, int p_materiau)
    {
	JsonObject piece = m_tabPiecesArmures.getJsonObject(p_idPiece);
	JsonArray tabPoints = piece.getJsonArray("points");
	return tabPoints.getInt(p_materiau);
    }

    /**
     *
     * @param p_idPiece
     * @return le nombre de points d'armure d'un bouclier
     */
    public int getPtsBouclier(int p_idPiece)
    {
	JsonObject objetIntermediaire = m_tabBoucliers.getJsonObject(p_idPiece);
	return objetIntermediaire.getInt("points");
    }

    /**
     * @param p_nbPts les points d'armure à modifier
     * @param p_typeArme type d'arme considéré
     * @param p_typeArmure type d'armure considéré
     * @return les points d'armure à effectivement utiliser face à un type
     * d'arme donné en fonction du type d'armure
     */
    private int getPtsArmureEffectifs(int p_nbPts, int p_typeArme, int p_typeArmure)
    {
	JsonArray tabPourType = m_tableAjustementArmure.getJsonArray(p_typeArmure);
	double coeff = tabPourType.getJsonNumber(p_typeArme).doubleValue();

	double preResult = (((double) p_nbPts) * coeff);
	long IntResult = Math.round(preResult);
	return (int) IntResult;
    }

    /**
     *
     * @param p_points les points d'armure
     * @return le rang d'armure
     */
    private int getRang(int p_points)
    {
	int i = -1;//on commence avec i en dehors du tableau (0 points d'armure, pas de bonus) et l'on teste si on peut l'augmenter, quand on ne peut plus l'augmenter on le renvoie.
	while (i <= 5 && p_points >= m_tableArmureRangs.getInt(i + 1))
	{
	    ++i;
	}
	return (i);
    }
}
