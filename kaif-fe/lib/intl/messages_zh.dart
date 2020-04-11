//please sync to messages_zh.properties
library message_zh;

import 'package:intl/intl.dart';
import 'package:intl/message_lookup_by_library.dart';

MessageLookupByLibrary get messages => new MessageLookup();

class MessageLookup extends MessageLookupByLibrary {

  get localeName => 'zh';

  final messages = {

    "error.title": () => Intl.message("無法處理"),
    "error.subtitle": () => Intl.message("抱歉，出錯了"),
    "error.status": () => Intl.message("狀態\:"),
    "error.message": () => Intl.message("訊息\:"),
    "email.activation.title": () => Intl.message("kaif 帳號啟用"),
    "email.activation.greeting": (a0) => Intl.message("您好，${a0}"),
    "email.activation.text1": () =>
        Intl.message("您的 kaif 帳號已經成功建立，請點擊下列連結完成帳號啟用"),
    "email.activation.text2": () => Intl.message("謝謝"),
    "email.reset-password.title": () => Intl.message("kaif 帳號密碼重置"),
    "email.reset-password.greeting": (a0) => Intl.message("您好，${a0}"),
    "email.reset-password.text1": () => Intl.message("您剛才申請密碼重置，請點擊下列連結完成重置"),
    "email.reset-password.text2": () => Intl.message("謝謝"),
    "email.signature": () => Intl.message("kaif 團隊"),
    "rest-error.RuntimeException": (a0) => Intl.message("伺服器內部錯誤\: ${a0}"),
    "rest-error.QueryTimeoutException": () => Intl.message("處理逾時"),
    "rest-error.PessimisticLockingFailureException": () =>
        Intl.message("資源無法存取"),
    "rest-error.PermissionDeniedDataAccessException": () =>
        Intl.message("權限不足"),
    "rest-error.OptimisticLockingFailureException": () =>
        Intl.message("資源暫時無法存取"),
    "rest-error.MethodArgumentNotValidException": (a0) =>
        Intl.message("格式不符。 ${a0}"),
    "rest-error.DuplicateKeyException": () => Intl.message("資料已經存在"),
    "rest-error.DataAccessException": (a0) => Intl.message("無法存取資料\: ${a0}"),
    "rest-error.DataIntegrityViolationException": () => Intl.message("資料破損"),
    "rest-error.Exception": (a0) => Intl.message("伺服器內部錯誤\: ${a0}"),
    "rest-error.RestAccessDeniedException": () =>
        Intl.message("權限不足。請確認帳號已經啟用，再登入重試。"),
    "sign-up.available": () => Intl.message("&\#10003; 帳號可申請"),
    "sign-up.name-already-taken": () => Intl.message("帳號已被申請\!"),
    "sign-up.invalid-name": () => Intl.message("帳號格式不對"),
    "sign-up.email-already-taken": () => Intl.message("Email 已被申請"),
    "sign-up.password-not-same": () => Intl.message("確認密碼不相同"),
    "account-menu.sign-up": () => Intl.message("註冊"),
    "account-menu.sign-in": () => Intl.message("登入"),
    "account-menu.sign-out": () => Intl.message("登出"),
    "unit-test": () => Intl.message("測試中"),
    "part-loader.permission-error": () => Intl.message("無權限存取網頁，請重新登入後再試一次"),
    "account-settings.reactivation-sent": () => Intl.message("確認信已經送出，請檢查你的信箱"),
    "account-settings.update-new-password-success": () =>
        Intl.message("修改密碼成功"),
    "account.OldPasswordNotMatchException": () => Intl.message("舊密碼不正確"),
    "account.AuthenticateFailException": () => Intl.message("驗證失敗，請確認帳號與密碼正確"),
    "rest-error.EmptyResultDataAccessException": () => Intl.message("查無此資料"),
    "article.min-title": (a0) => Intl.message("標題最少 ${a0} 字"),
    "article.create-success": () => Intl.message("文章已建立"),
    "debate.create-success": () => Intl.message("留言已發出"),
    "debate.edit-success": () => Intl.message("留言已修改"),
    "debate.min-content": (a0) => Intl.message("留言最少 ${a0} 字"),
    "email.password-was-reset.greeting": (a0) => Intl.message("您好，${a0}"),
    "email.password-was-reset.title": () => Intl.message("您的 kaif 密碼已變更"),
    "email.password-was-reset.text1": () => Intl.message("您的 kaif 密碼已經變更，"),
    "email.password-was-reset.text2": (a0) =>
        Intl.message("如果您對密碼受到改變沒有任何印象，請馬上到 ${a0} 重新執行忘記密碼並修改。"),
    "email.password-was-reset.text3": () => Intl.message("謝謝"),
    "votable.sign-in-to-vote": () => Intl.message("先登入後才能推文喔"),
    "article.min-content": (a0) => Intl.message("內文最少 ${a0} 字"),
    "kmark.preview": () => Intl.message("預覽"),
    "kmark.finish-preview": () => Intl.message("結束預覽"),
    "account-setting.update-description-success": () => Intl.message("關於我已更新"),
    "account-setting.min-description": (a0) => Intl.message("關於我最少需 ${a0} 字"),
    "account-menu.debate-replies": () => Intl.message("新的回應"),
    "debate.sign-in-to-debate": () => Intl.message("登入後才能討論喔"),
    "kmark.help": () => Intl.message("格式小抄"),
    "kmark.finish-help": () => Intl.message("關閉小抄"),
    "account-menu.settings": () => Intl.message("帳號設定"),
    "kmark.auto-link-placeholder": () => Intl.message("網頁說明"),
    "account-menu.news-feed": () => Intl.message("新消息"),
    "client-app.CallbackUriReservedException": () =>
        Intl.message("uri 不能包含保留字\: kaif"),
    "client-app.ClientAppNameReservedException": () =>
        Intl.message("App 名稱不能為 kaif"),
    "success": () => Intl.message("完成"),
    "client-app.ClientAppMaxException": (a0) =>
        Intl.message("最多建立 ${a0} 個應用程式"),
    "client-app-scope.public": () => Intl.message("讀取公開的文章與討論"),
    "client-app-scope.feed": () => Intl.message("讀取你的新消息"),
    "client-app-scope.user": () => Intl.message("個人基本資料"),
    "client-app-scope.article": () => Intl.message("讀取你的文章與張貼文章"),
    "client-app-scope.debate": () => Intl.message("讀取你的留言與發新的留言"),
    "client-app-scope.vote": () => Intl.message("文章與留言的投票"),
    "account.RequireCitizenException": () => Intl.message("您尚未完成帳號啟用，請啟用後再試一次"),
    "zone.invalid-zone": () => Intl.message("討論區名稱格式不符"),
    "zone.available": () => Intl.message("&\#10003; 討論區名稱可以使用"),
    "zone.zone-already-taken": () => Intl.message("討論區已被申請\!"),
    "zone.CreditNotEnoughException": () => Intl.message("你的聲望不足"),
    "zone.create-success": () => Intl.message("討論區已建立"),
    "article.force-create": () => Intl.message("不管重覆，我就是要分享"),
    "article.url-exist": () => Intl.message("該連結已經有人分享了")
  };
}