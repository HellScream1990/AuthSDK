package tech.jianyue.auth;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXMiniProgramObject;
import com.tencent.mm.opensdk.modelmsg.WXMusicObject;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXVideoObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONObject;

/**
 * 描述: 微信相关授权操作
 * 作者: WJ
 * 时间: 2018/1/19
 * 版本: 1.0
 */
public class AuthBuildForWX extends Auth.Builder {
    private IWXAPI mApi;                                                // 微信 Api
    private int mShareType = -100;                                      // 分享类型
    private String mID;                                                 // 小程序 ID
    private String mPath;                                               // 小程序 Path

    AuthBuildForWX(Context context) {
        super(context);
    }

    @Override                           // 初始化资源
    void init() {
        if (TextUtils.isEmpty(Auth.AuthBuilder.WECHAT_APPID)) {
            throw new IllegalArgumentException("WECHAT_APPID was empty");
        } else if (mApi == null) {
            mApi = WXAPIFactory.createWXAPI(mContext, Auth.AuthBuilder.WECHAT_APPID, true);
            mApi.registerApp(Auth.AuthBuilder.WECHAT_APPID);
        }
    }

    @Override                           // 清理资源
    void destroy() {
        super.destroy();
        if (mApi != null) {
            mApi.detach();
            mApi = null;
        }
    }

    IWXAPI getWXApi() {
        return mApi;
    }

    @Override
    public AuthBuildForWX setAction(@Auth.ActionWX int action) {
        mAction = action;
        return this;
    }

    public AuthBuildForWX shareToSession() {
        mShareType = SendMessageToWX.Req.WXSceneSession;
        return this;
    }

    public AuthBuildForWX shareToTimeline() {
        mShareType = SendMessageToWX.Req.WXSceneTimeline;
        return this;
    }

    public AuthBuildForWX shareToFavorite() {
        mShareType = SendMessageToWX.Req.WXSceneFavorite;
        return this;
    }

    public AuthBuildForWX shareText(String text) {
        mText = text;
        return this;
    }

    public AuthBuildForWX shareTextTitle(String title) {
        mTitle = title;
        return this;
    }

    public AuthBuildForWX shareTextDescription(String description) {
        mDescription = description;
        return this;
    }

    public AuthBuildForWX shareImage(Bitmap bitmap) {              // imageData 大小限制为 10MB
        mBitmap = bitmap;
        return this;
    }

    public AuthBuildForWX shareImageTitle(String title) {
        mTitle = title;
        return this;
    }

    public AuthBuildForWX shareMusicTitle(String title) {
        mTitle = title;
        return this;
    }

    public AuthBuildForWX shareMusicDescription(String description) {
        mDescription = description;
        return this;
    }

    public AuthBuildForWX shareMusicImage(Bitmap bitmap) {
        mBitmap = bitmap;
        return this;
    }

    /**
     * 网络链接
     */
    public AuthBuildForWX shareMusicUrl(String url) {
        mUrl = url;
        return this;
    }

    public AuthBuildForWX shareLinkTitle(String title) {
        mTitle = title;
        return this;
    }

    public AuthBuildForWX shareLinkDescription(String description) {
        mDescription = description;
        return this;
    }

    public AuthBuildForWX shareLinkImage(Bitmap bitmap) {
        mBitmap = bitmap;
        return this;
    }

    /**
     * 网络链接
     */
    public AuthBuildForWX shareLinkUrl(String url) {
        mUrl = url;
        return this;
    }

    public AuthBuildForWX shareVideoTitle(String title) {
        mTitle = title;
        return this;
    }

    public AuthBuildForWX shareVideoDescription(String description) {
        mDescription = description;
        return this;
    }

    public AuthBuildForWX shareVideoImage(Bitmap bitmap) {
        mBitmap = bitmap;
        return this;
    }

    /**
     * 网络链接
     */
    public AuthBuildForWX shareVideoUrl(String url) {
        mUrl = url;
        return this;
    }

    public AuthBuildForWX shareProgramTitle(String title) {             // 分享小程序
        mTitle = title;
        return this;
    }

    public AuthBuildForWX shareProgramDescription(String description) {
        mDescription = description;
        return this;
    }

    public AuthBuildForWX shareProgramImage(Bitmap bitmap) {
        mBitmap = bitmap;
        return this;
    }

    /**
     * 低版本微信打开的网络链接
     */
    public AuthBuildForWX shareProgramUrl(String url) {
        mUrl = url;
        return this;
    }

    public AuthBuildForWX shareProgramId(String id) {
        mID = id;
        return this;
    }

    public AuthBuildForWX shareProgramPath(String path) {
        mPath = path;
        return this;
    }

    @Override
    public void build(AuthCallback callback) {
        super.build(callback);
        if (!mApi.isWXAppInstalled()) {
            mCallback.onFailed("未安装微信客户端");
            destroy();
        } else if (mAction != Auth.LOGIN && mShareType == -100) {
            mCallback.onFailed("必须添加分享类型, 使用 shareToSession(),shareToTimeline(),shareToFavorite() ");
            destroy();
        } else {
            switch (mAction) {
                case Auth.LOGIN:
                    login();
                    break;
                case Auth.SHARE_TEXT:
                    shareText();
                    break;
                case Auth.SHARE_IMAGE:
                    shareBitmap();
                    break;
                case Auth.SHARE_LINK:
                    shareLink();
                    break;
                case Auth.SHARE_VIDEO:
                    shareVideo();
                    break;
                case Auth.SHARE_MUSIC:
                    shareMusic();
                    break;
                case Auth.SHARE_PROGRAM:
                    shareProgram();
                    break;
                default:
                    mCallback.onFailed("微信暂未支持的 Action, 或未定义 Action");
                    break;
            }
        }
    }

    private void shareText() {
        // 分享文本到微信, 文本描述，分享到朋友圈可以不传,聊天界面和收藏必须传
        if (TextUtils.isEmpty(mText)) {
            mCallback.onFailed("必须添加文本, 使用 shareText(str) ");
        } else if (mShareType != SendMessageToWX.Req.WXSceneTimeline && TextUtils.isEmpty(mDescription)) {
            mCallback.onFailed("必须添加文本描述, 使用 shareDescription(str) ");
        } else {
            WXMediaMessage msg = new WXMediaMessage();
            msg.mediaObject = new WXTextObject(mText);
            msg.description = mDescription;
            msg.title = mTitle;

            share(msg);
        }
    }

    private void shareBitmap() {
        if (mBitmap == null) {
            mCallback.onFailed("必须添加 Bitmap, 且不为空, 使用 shareImage(bitmap) ");
        } else {
            // imageData 大小限制为 10MB, 缩略图大小限制为 32K
            Bitmap thumbBmp = Bitmap.createScaledBitmap(mBitmap, 150, 150, true);

            WXMediaMessage msg = new WXMediaMessage();
            msg.mediaObject = new WXImageObject(mBitmap);
            msg.thumbData = Utils.bmpToByteArray(thumbBmp, false);
            msg.title = mTitle;

            share(msg);
        }
    }

    private void shareMusic() {
        if (TextUtils.isEmpty(mUrl)) {
            mCallback.onFailed("必须添加音乐链接, 且不为空, 使用 shareMusicUrl(url) ");
        } else if (mBitmap == null) {
            mCallback.onFailed("必须添加音乐缩略图, 且不为空, 使用 shareMusicImage(bitmap) ");
        } else if (mTitle == null) {
            mCallback.onFailed("必须添加音乐标题, 使用 shareMusicTitle(title) ");
        } else {
            Bitmap thumbBmp = Bitmap.createScaledBitmap(mBitmap, 150, 150, true);
            WXMusicObject musicObject = new WXMusicObject();
            musicObject.musicUrl = mUrl;                                            // 音乐链接

            WXMediaMessage msg = new WXMediaMessage();
            msg.mediaObject = musicObject;
            msg.title = mTitle;                                                     // 音乐标题，必传，但是可是是空字符串
            msg.description = mDescription;                                         // 音乐描述，可不传
            msg.thumbData = Utils.bmpToByteArray(thumbBmp, false);      // 缩略图大小限制为32K

            share(msg);
        }
    }

    private void shareLink() {
        if (TextUtils.isEmpty(mUrl)) {
            mCallback.onFailed("必须添加链接, 且不为空, 使用 shareLinkUrl(url) ");
        } else if (mBitmap == null) {
            mCallback.onFailed("必须添加链接缩略图, 且不为空, 使用 shareLinkImage(bitmap) ");
        } else if (mTitle == null) {
            mCallback.onFailed("必须添加链接标题, 使用 shareLinkTitle(title) ");
        } else {
            Bitmap thumbBmp = Bitmap.createScaledBitmap(mBitmap, 150, 150, true);
            WXWebpageObject webObject = new WXWebpageObject();
            webObject.webpageUrl = mUrl;

            WXMediaMessage msg = new WXMediaMessage();
            msg.mediaObject = webObject;
            msg.title = mTitle;
            msg.description = mDescription;
            msg.thumbData = Utils.bmpToByteArray(thumbBmp, false);      // 缩略图大小限制为32K

            share(msg);
        }
    }

    private void shareVideo() {
        if (TextUtils.isEmpty(mUrl)) {
            mCallback.onFailed("必须添加视频链接, 且不为空, 使用 shareVideoUrl(url) ");
        } else if (mBitmap == null) {
            mCallback.onFailed("必须添加视频缩略图, 且不为空, 使用 shareVideoImage(bitmap) ");
        } else if (mTitle == null) {
            mCallback.onFailed("必须添加视频标题, 使用 shareVideoTitle(title) ");
        } else {
            Bitmap thumbBmp = Bitmap.createScaledBitmap(mBitmap, 150, 150, true);
            WXVideoObject videoObject = new WXVideoObject();
            videoObject.videoUrl = mUrl;                                            // 视频链接

            WXMediaMessage msg = new WXMediaMessage();
            msg.mediaObject = videoObject;
            msg.title = mTitle;
            msg.description = mDescription;
            msg.thumbData = Utils.bmpToByteArray(thumbBmp, false);      // 缩略图大小限制为32K

            share(msg);
        }
    }

    private void shareProgram() {
        if (TextUtils.isEmpty(mUrl)) {
            mCallback.onFailed("必须添加小程序链接, 且不为空, 使用 shareProgramUrl(url) ");
        } else if (TextUtils.isEmpty(mID)) {
            mCallback.onFailed("必须添加小程序ID, 使用 shareProgramId(id) ");
        } else if (TextUtils.isEmpty(mPath)) {
            mCallback.onFailed("必须添加小程序Path, 使用 shareProgramPath(path) ");
        } else if (mBitmap == null) {
            mCallback.onFailed("必须添加小程序缩略图, 且不为空, 使用 shareProgramImage(bitmap) ");
        } else if (mTitle == null) {
            mCallback.onFailed("必须添加小程序标题, 使用 shareProgramTitle(title) ");
        } else if (mShareType != SendMessageToWX.Req.WXSceneSession) {
            mCallback.onFailed("目前只支持分享到会话 ");
        } else {
            Bitmap thumbBmp = Bitmap.createScaledBitmap(mBitmap, 150, 150, true);
            WXMiniProgramObject programObject = new WXMiniProgramObject();
            programObject.webpageUrl = mUrl;                                        // 低版本微信打开该 url
            programObject.userName = mID;                                           // 跳转小程序的原始 ID
            programObject.path = mPath;                                             // 小程序的Path

            WXMediaMessage msg = new WXMediaMessage();
            msg.mediaObject = programObject;
            msg.title = mTitle;
            msg.description = mDescription;
            msg.thumbData = Utils.bmpToByteArray(thumbBmp, false);      // 缩略图大小限制为32K

            share(msg);
        }
    }

    private void share(WXMediaMessage msg) {
        if (msg == null) {
            mCallback.onFailed("分享失败, 内部错误");
        } else {
            SendMessageToWX.Req req = new SendMessageToWX.Req();
            req.transaction = Sign;                                                 // 用于唯一标识一个请求
            req.scene = mShareType;
            req.message = msg;
            mApi.sendReq(req);
        }
    }

    private void login() {                                                  // 微信登录, 1 获取微信 code
        if (mApi.isWXAppInstalled()) {
            SendAuth.Req req = new SendAuth.Req();
            req.scope = "snsapi_userinfo";
            req.state = Sign;
            req.transaction = Sign;
            mApi.sendReq(req);
        } else {
            mCallback.onFailed("未安装微信客户端");
        }
    }

    void getInfo(String code) {                                             // 通过 AuthActivity 调用
        new AuthBuildForWX.GetInfo(mCallback).execute(code);
    }

    private static class GetInfo extends AsyncTask<String, Void, UserInfoForThird> {
        private AuthCallback callback;                                      // 回调函数

        GetInfo(AuthCallback callback) {
            this.callback = callback;
        }

        @Override
        protected UserInfoForThird doInBackground(String... strings) {
            try {
                String j2 = getToken(strings[0]);
                JSONObject object2 = new JSONObject(j2);
                String refresh_token = object2.getString("refresh_token");

                String j3 = refreshToken(refresh_token);
                JSONObject object3 = new JSONObject(j3);
                String access_token = object3.getString("access_token");
                String openid = object3.getString("openid");
                long expires_in = object3.getLong("expires_in");

                if (checkToken(access_token, openid)) {
                    return new UserInfoForThird().initForWX(getUserInfo(access_token, openid), access_token, refresh_token, openid, expires_in);
                }
                return null;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(UserInfoForThird info) {
            super.onPostExecute(info);
            if (info != null) {
                callback.onSuccessForLogin(info);
            } else {
                callback.onFailed("微信登录失败");
            }
            callback = null;
        }

        // 微信登录, 2 通过 code 获取 refresh_token
        private String getToken(String code) throws Exception {
            String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="
                    + Auth.AuthBuilder.WECHAT_APPID
                    + "&secret="
                    + Auth.AuthBuilder.WECHAT_SECRET
                    + "&code="
                    + code
                    + "&grant_type=authorization_code";
            return Utils.get(url);
        }

        // 微信登录, 3 通过 refresh_token 刷新 access_token
        private String refreshToken(String token) throws Exception {
            String url = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid="
                    + Auth.AuthBuilder.WECHAT_APPID
                    + "&grant_type=refresh_token"
                    + "&refresh_token="
                    + token;
            return Utils.get(url);
        }

        // 微信登录, 4 检验授权凭证（access_token）是否有效
        private boolean checkToken(String token, String openId) throws Exception {
            String url = "https://api.weixin.qq.com/sns/auth?access_token="
                    + token
                    + "&openid="
                    + openId;
            JSONObject object = new JSONObject(Utils.get(url));
            return object.getInt("errcode") == 0;
        }

        // 微信登录, 5 获取用户信息
        private String getUserInfo(String token, String openId) throws Exception {
            String url = "https://api.weixin.qq.com/sns/userinfo?access_token="
                    + token
                    + "&openid="
                    + openId;
            return Utils.get(url);
        }
    }
}