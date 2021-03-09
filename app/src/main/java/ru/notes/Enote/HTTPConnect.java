package ru.notes.Enote;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HTTPConnect {
      //Отправка Get-запроса без куков (для первоначальной авторизации, и взятия куков сессии)
    public static String Send(String [] urlServer) throws Exception
    {
        try {
            StringBuilder uri = new StringBuilder();

            for (String str : urlServer)
                uri.append(str);

            URL url = new URL(uri.toString());

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String message;
            StringBuffer response = new StringBuffer();

            while ((message = bufferedReader.readLine()) != null) {
                response.append(message);
            }
            bufferedReader.close();
            //Cookies
            String cookiesHeader = connection.getHeaderField("Set-Cookie");
            List<HttpCookie> cookies = HttpCookie.parse(cookiesHeader);
            CookieManager cookieManager = new CookieManager();
            for (HttpCookie cookie : cookies) {
                cookieManager.getCookieStore().add(null, cookie);
            }
             response.append(cookieManager.getCookieStore().getCookies().toString());

            return response.toString();
        }
        catch (Exception e)
        {
            return null;
        }
    }
    //Отправка Get-запроса с куками сессии
    public static String SendWithCookie(String [] urlServer) throws Exception
    {
        try {
            String cookieSession = urlServer[urlServer.length-1];

            StringBuilder uri = new StringBuilder();

            for(int i = 0; i<urlServer.length-1;++i)
                uri.append(urlServer[i]);

            URL url = new URL(uri.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            connection.setRequestProperty("Cookie", cookieSession);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String message;
            StringBuffer response = new StringBuffer();

            while ((message = bufferedReader.readLine()) != null) {
                response.append(message);
            }
            bufferedReader.close();

            String cookiesHeader = connection.getHeaderField("Set-Cookie");
            if(cookiesHeader!=null) {
                List<HttpCookie> cookies = HttpCookie.parse(cookiesHeader);
                CookieManager cookieManager = new CookieManager();
                for (HttpCookie cookie : cookies) {
                    cookieManager.getCookieStore().add(null, cookie);
                }
                response.append(cookieManager.getCookieStore().getCookies().toString());
            }
            return response.toString();
        }
        catch (Exception e)
        {
            return null;
        }
    }
     //Метод взятия статуса из ответа сервера
    public static int takeStatus(String message)
    {
        String [] stage1 = message.split(": ");
        String[] response=stage1[1].split(", ");
        return Integer.parseInt(response[0]);
    }
    //Метод взятия id заметок из ответа сервера
    public static int takeIdNote(String message)
    {
    String[] stage1 = message.split("message\": \"");
    StringBuilder stringBuilder = new StringBuilder();
        for (char s: stage1[1].toCharArray()) {
            if((int)s>=(int)'0' && (int)s<=(int)'9')
                stringBuilder.append(s);
        }
    return Integer.parseInt(stringBuilder.toString());
    }
    //Метод взятия куков из ответа сервера
    public static String takeCookies(String message)
    {
        if(message.contains("SESSION"))
        {
            String[] result = message.split("\\[");
            return result[1].substring(0, result[1].length() - 1);
        }
        else
                return null;
    }
    //Метод взятия всех заметок из ответа сервера
    public static ArrayList<String> takeTextNotes(String message)
    {
        ArrayList<String> Listnotes = new ArrayList<String>();
        //{"status": 1, "message": "[]"}
        if(message.contains("[]"))
        {
            return null;
        }
            else{
        String responsenotes[] = message.split("\\[\\{\\\\\"id\\\\\":");
        String resultanswer = responsenotes[1].substring(0, responsenotes[1].length() - 6);
        String[] stage1 = resultanswer.split("\\\\\"\\},\\{\\\\\"id\\\\\":");
        for (String strline : stage1) {
            String[] stage2 = strline.split(",\\\\\"text\\\\\":\\\\");
            Listnotes.add(stage2[0]);
            Listnotes.add(stage2[1].substring(1));
        }
        return Listnotes;
    }
    }
}
