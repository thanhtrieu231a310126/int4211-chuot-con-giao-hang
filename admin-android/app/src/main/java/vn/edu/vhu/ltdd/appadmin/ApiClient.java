package vn.edu.vhu.ltdd.appadmin;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * ApiClient: Quản lý kết nối tới máy chủ Render của nhóm.
 */
public class ApiClient {

    // Đường link máy chủ Render của nhóm Chuột Con
    private static final String BASE_URL = "https://chuot-con-backend.onrender.com/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()) // Tự động chuyển JSON thành đối tượng Java
                    .build();
        }
        return retrofit;
    }
}