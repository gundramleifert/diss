package de.planet.diss_leifert.util;

import com.achteck.misc.types.charmap.IntCharMap;

import java.util.ArrayList;
import java.util.List;

public class ConfMatUtil {
    public static de.uros.citlab.confmat.ConfMat convert(com.achteck.misc.types.ConfMat cm) {
        de.uros.citlab.confmat.CharMap cmNew = new de.uros.citlab.confmat.CharMap();
        IntCharMap charMap = cm.getCharMap();
        int size = charMap.keySet().size();
        for (int i = 1; i < size; i++) {
            cmNew.add(charMap.get(i));
        }
        if (cmNew.size() < cm.getDoubleMat()[0].length) {
            char cAdd = '°';
            while (cmNew.containsChar(cAdd)) {
                cAdd++;
            }
//            System.out.println("add character '" + cAdd + "' as unknown character.");
            cmNew.add(cAdd);
        }
        return new de.uros.citlab.confmat.ConfMat(cmNew, cm.getDoubleMat());
    }

//    public static de.uros.citlab.confmat.ConfMat convert(ConfMat cm) {
//        CharMap cmNew = new CharMap();
//        IntCharMap charMap = cm.getCharMap();
//        int size = charMap.keys().length;
//        for (int i = 1; i < size; i++) {
//            cmNew.add(String.valueOf(charMap.getValue(i)));
//        }
//        return new de.uros.citlab.confmat.ConfMat(cmNew, cm.getDoubleMat());
//    }

    public static List<de.uros.citlab.confmat.ConfMat> convert(List<com.achteck.misc.types.ConfMat> cms) {
        List<de.uros.citlab.confmat.ConfMat> res = new ArrayList<>(cms.size());
        for (int i = 0; i < cms.size(); i++) {
            res.add(convert(cms.get(i)));
        }
        return res;
    }


}
