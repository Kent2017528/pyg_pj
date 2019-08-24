import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.Map;
import java.util.Set;

public class T1 {

    public static void main(String[] args) {

        String s="{\"机身内存\":\"16G\",\"网络\":\"联通2G\"}";
        StringBuffer title=new StringBuffer("华为");
        JSONObject jsonObject = JSON.parseObject(s);
        Map<String,String> map = JSONObject.toJavaObject(jsonObject, Map.class);
        Set<String> strings = map.keySet();
        for (String s1:strings){
            title.append(" "+map.get(s1));

            //System.out.println(map.get(s1));
        }
        System.out.println(title.toString());

    }
}
