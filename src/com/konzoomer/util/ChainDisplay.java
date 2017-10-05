package com.konzoomer.util;

import com.konzoomer.R;
import com.konzoomer.domain.Chains;

/**
 * Created by IntelliJ IDEA.
 * User: Torben Vesterager
 * Date: 14-12-2010
 * Time: 14:00:13
 */
public class ChainDisplay {

    public static String getName(short chainID) {
        switch (chainID) {
            case Chains.ID_SUPERBRUGSEN:
                return "SuperBrugsen";
            case Chains.ID_DAGLI_BRUGSEN:
                return "Dagli'Brugsen";
            case Chains.ID_KVICKLY:
                return "Kvickly";
            case Chains.ID_FAKTA:
                return "Fakta";
            case Chains.ID_IRMA:
                return "Irma";
            case Chains.ID_BILKA:
                return "Bilka";
            case Chains.ID_FOTEX:
                return "Føtex";
            case Chains.ID_NETTO:
                return "Netto";
            case Chains.ID_SPAR:
                return "Spar";
            case Chains.ID_KWIK_SPAR:
                return "Kwik Spar";
            case Chains.ID_SUPER_SPAR:
                return "Super Spar";
            case Chains.ID_EUROSPAR:
                return "EuroSpar";
            case Chains.ID_ABC_LAVPRIS:
                return "ABC Lavpris";
            case Chains.ID_ALDI:
                return "Aldi";
            case Chains.ID_KIWI_MINIPRIS:
                return "KIWI minipris";
            case Chains.ID_LIDL:
                return "Lidl";
            case Chains.ID_LOVBJERG:
                return "Løvbjerg";
            case Chains.ID_REMA_1000:
                return "REMA 1000";
            case Chains.ID_SUPERBEST:
                return "SuperBest";
            default:
                return "";
        }
    }

    public static int getIconID(short chainID) {
        switch (chainID) {
            case Chains.ID_SUPERBRUGSEN:
                return R.drawable.ic_chain_superbrugsen;
            case Chains.ID_DAGLI_BRUGSEN:
                return R.drawable.ic_chain_dagli_brugsen;
            case Chains.ID_KVICKLY:
                return R.drawable.ic_chain_kvickly;
            case Chains.ID_FAKTA:
                return R.drawable.ic_chain_fakta;
            case Chains.ID_IRMA:
                return R.drawable.ic_chain_irma;
            case Chains.ID_BILKA:
                return R.drawable.ic_chain_bilka;
            case Chains.ID_FOTEX:
                return R.drawable.ic_chain_fotex;
            case Chains.ID_NETTO:
                return R.drawable.ic_chain_netto;
            case Chains.ID_SPAR:
                return R.drawable.ic_chain_spar;
            case Chains.ID_KWIK_SPAR:
                return R.drawable.ic_chain_kwik_spar;
            case Chains.ID_SUPER_SPAR:
                return R.drawable.ic_chain_super_spar;
            case Chains.ID_EUROSPAR:
                return R.drawable.ic_chain_eurospar;
            case Chains.ID_ABC_LAVPRIS:
                return R.drawable.ic_chain_abc_lavpris;
            case Chains.ID_ALDI:
                return R.drawable.ic_chain_aldi;
            case Chains.ID_KIWI_MINIPRIS:
                return R.drawable.ic_chain_kiwi_minipris;
            case Chains.ID_LIDL:
                return R.drawable.ic_chain_lidl;
            case Chains.ID_LOVBJERG:
                return R.drawable.ic_chain_lovbjerg;
            case Chains.ID_REMA_1000:
                return R.drawable.ic_chain_rema_1000;
            case Chains.ID_SUPERBEST:
                return R.drawable.ic_chain_superbest;
            default:
                return R.drawable.expander_ic_minimized;
        }
    }
}
