/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.duckdns.spacedock.upengine.libupsystem;

import java.util.ArrayList;
import org.duckdns.spacedock.upengine.libupsystem.Armure.PieceArmure;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author iconoctopus
 */
public class ArmureTest
{

    static Armure.PieceArmure piece1;
    static Armure.PieceArmure piece2;
    static Armure.PieceArmure piece3;
    static Armure.PieceArmure piece4;
    static Armure.PieceArmure piece5;
    static ArrayList<PieceArmure> listPieces = new ArrayList<>();
    static Armure armure;

    @BeforeClass
    public static void setUpClass()
    {
	piece1 = new Armure.PieceArmure(0, 0, 0);
	piece2 = new Armure.PieceArmure(0, 1, 3);
	piece3 = new Armure.PieceArmure(7, 2, 0);
	piece4 = new Armure.PieceArmure(7, 3, 3);
	piece5 = new Armure.PieceArmure(4, 0, 1);

	listPieces.add(piece1);
	listPieces.add(piece2);
	listPieces.add(piece3);
	listPieces.add(piece4);

    }

    @Before
    public void setUp()
    {
	armure = new Armure(listPieces);
    }

    @After
    public void tearDown()
    {
    }

    @Test
    public void testPieceArmure()
    {
	Assert.assertEquals(0, piece1.getIdPiece());
	Assert.assertEquals(0, piece1.getMalusEsquive());
	Assert.assertEquals(0, piece1.getMalusParade());
	Assert.assertEquals(0, piece1.getMateriau());
	Assert.assertEquals(3, piece1.getNbpoints());
	Assert.assertEquals(0, piece1.getType());
	Assert.assertEquals(1, piece1.getnbMax());
	Assert.assertEquals("heaume complet en plates", piece1.toString());

	Assert.assertEquals(0, piece2.getIdPiece());
	Assert.assertEquals(0, piece2.getMalusEsquive());
	Assert.assertEquals(0, piece2.getMalusParade());
	Assert.assertEquals(3, piece2.getMateriau());
	Assert.assertEquals(1, piece2.getNbpoints());
	Assert.assertEquals(1, piece2.getType());
	Assert.assertEquals(1, piece2.getnbMax());
	Assert.assertEquals("heaume complet en cuir bouilli", piece2.toString());

	Assert.assertEquals(7, piece3.getIdPiece());
	Assert.assertEquals(10, piece3.getMalusEsquive());
	Assert.assertEquals(0, piece3.getMalusParade());
	Assert.assertEquals(0, piece3.getMateriau());
	Assert.assertEquals(6, piece3.getNbpoints());
	Assert.assertEquals(2, piece3.getType());
	Assert.assertEquals(1, piece3.getnbMax());
	Assert.assertEquals("cuirasse en plates", piece3.toString());

	Assert.assertEquals(7, piece4.getIdPiece());
	Assert.assertEquals(1, piece4.getMalusEsquive());
	Assert.assertEquals(0, piece4.getMalusParade());
	Assert.assertEquals(3, piece4.getMateriau());
	Assert.assertEquals(2, piece4.getNbpoints());
	Assert.assertEquals(3, piece4.getType());
	Assert.assertEquals(1, piece4.getnbMax());
	Assert.assertEquals("cuirasse en cuir bouilli", piece4.toString());

	Assert.assertEquals(4, piece5.getIdPiece());
	Assert.assertEquals(0, piece5.getMalusEsquive());
	Assert.assertEquals(2, piece5.getMalusParade());
	Assert.assertEquals(1, piece5.getMateriau());
	Assert.assertEquals(2, piece5.getNbpoints());
	Assert.assertEquals(0, piece5.getType());
	Assert.assertEquals(2, piece5.getnbMax());
	Assert.assertEquals("brassière en lamelles ou maille", piece5.toString());
    }

    @Test
    public void testArmure()
    {

	Assert.assertEquals(10, armure.getMalusEsquive());
	Assert.assertEquals(0, armure.getMalusParade());
    }

    @Test
    public void testGetBonusND()
    {

	Assert.assertEquals(15, armure.getBonusND(0));

    }

    @Test
    public void testGetRedDegats()
    {
	Assert.assertEquals(0, armure.getRedDegats(4));
    }

    @Test
    public void testAddPiece()
    {
	armure.addPiece(piece5);
	Assert.assertEquals(0, armure.getRedDegats(4));
	Assert.assertEquals(10, armure.getRedDegats(1));
	Assert.assertEquals(15, armure.getBonusND(0));
	Assert.assertEquals(10, armure.getMalusEsquive());
	Assert.assertEquals(2, armure.getMalusParade());
	armure.addPiece(piece5);
	Assert.assertEquals(4, armure.getMalusParade());
    }

}