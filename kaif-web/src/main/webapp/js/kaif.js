// Object.create for IE8 (https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Object/create)
if (typeof Object.create != 'function') {
  Object.create = (function() {
    var Object = function() {};
    return function (prototype) {
      if (arguments.length > 1) {
        throw Error('Second argument not supported');
      }
      if (typeof prototype != 'object') {
        throw TypeError('Argument must be an object');
      }
      Object.prototype = prototype;
      var result = new Object();
      Object.prototype = null;
      return result;
    };
  })();
}


var KaifWeb = function(config) {

    var ServerType = function(baseUrl, baseAccountUrl) {
        this.baseUrl = baseUrl;
        this.baseAccountUrl = baseAccountUrl;
    };
    ServerType.prototype.getUrl = function(url) {
        return this.baseUrl + url;
    };
    ServerType.prototype.getAccountUrl = function(url) {
        return this.baseAccountUrl + url;
    };

    var AccountService = function(serverType) {
        this.serverType = serverType;
    };
    AccountService.prototype.createAccount = function(name, email, password, callback) {
        $.ajax({
            type   : 'PUT',
            url    : this.serverType.getAccountUrl('/'),
            contentType: "application/json; charset=utf-8",
            data   : JSON.stringify({'name': name, 'email': email ,'password': password}),
            dataType: "json",
            success: function(data) { callback(data); },
            error  : function() { callback(null); }
        });
    };
    AccountService.prototype.authenticate = function(name, password, callback) {
        $.ajax({
            type   : 'POST',
            url    : this.serverType.getAccountUrl('/authenticate'),
            contentType: "application/json; charset=utf-8",
            data   : JSON.stringify({'name': name,'password': password}),
            dataType: "json",
            success: function(data) { callback(data); },
            error  : function() { callback(null); }
        });
    };
    AccountService.prototype.extendsAccessToken = function(account, callback) {
        $.ajax({
            type   : 'POST',
            headers: {'X-KAIF-ACCESS-TOKEN':account.accessToken},
            url    : this.serverType.getAccountUrl('/extends-access-token'),
            contentType: "application/json; charset=utf-8",
            data   : JSON.stringify({accountId: account.accountId}), // empty post result in 403 forbidden
            dataType: "json",
            success: function(data) { callback(data); },
            error  : function() { callback(null); }
        });
    };

    var AccountManager = function(accountService) {
        this.accountService = accountService;
        this.storage = null;
    }
    AccountManager.ACCOUNT_KEY = 'ACCOUNT';
    AccountManager.EXTEND_INTERVAL = 1000*3600*24; // 1 day
    AccountManager.prototype.saveAccount = function(accountAuth) {
        // pick localStorage or sessionStorage according to rememberMe
        var account = {};
        account.name = accountAuth.name;
        account.accountId = accountAuth.accountId ;
        account.accessToken = accountAuth.accessToken;
        account.expireTime = accountAuth.expireTime;
        account.lastExtend = new Date().getTime();
        this.storage.setItem(AccountManager.ACCOUNT_KEY, JSON.stringify(account));
    };
    AccountManager.prototype.removeAccount = function() { // clear both localStorage and sessionStorage
        localStorage.removeItem(AccountManager.ACCOUNT_KEY);
        sessionStorage.removeItem(AccountManager.ACCOUNT_KEY);
    };
    AccountManager.prototype.loadAccountFromStorage = function(storage) {
        var accountJson = storage.getItem(AccountManager.ACCOUNT_KEY);
        if (!accountJson) {
            return null;
        }
        this.storage = storage;
        return $.parseJSON(accountJson);
    }
    AccountManager.prototype.loadAccount = function() { // load from localStorage first then sessionStorage
        var account = this.loadAccountFromStorage(localStorage) || this.loadAccountFromStorage(sessionStorage);
        if (!account) {
            return null;
        }

        if (new Date().getTime() > account.expireTime) {
            this.removeAccount();
            return null;
        }

        if (new Date().getTime() - account.lastExtend > AccountManager.EXTEND_INTERVAL) {
            this.extendsAccessToken(account);
        }

        return account;
    };

    AccountManager.prototype.signUp = function(name, email, password, confirmPassword, callback) {
        //TODO verify password == confirmPw
        this.accountService.createAccount(name, email, password, $.proxy(function(ignore) {
            callback(true);
        }, this));
    };
    AccountManager.prototype.logout = function() {
        this.removeAccount();
    };
    AccountManager.prototype.login = function(name, password, rememberMe, callback) {
        this.storage = rememberMe ? localStorage : sessionStorage;
        this.accountService.authenticate(name, password, $.proxy(function(accountAuth) {
            if (accountAuth && 'accountId'   in accountAuth
                            && 'accessToken' in accountAuth
                            && 'name'        in accountAuth
                            && 'expireTime'  in accountAuth) {
                this.saveAccount(accountAuth);
                callback(true);
            } else {
                callback(false);
            }
        }, this));
    };
    AccountManager.prototype.extendsAccessToken = function(account) {
        this.accountService.extendsAccessToken(account, $.proxy(function(accountAuth) {
            if (accountAuth && 'accountId'   in accountAuth
                            && 'accessToken' in accountAuth
                            && 'name'        in accountAuth
                            && 'expireTime'  in accountAuth) {
                this.saveAccount(accountAuth);
            }
        }, this));
    };

    var AccountController = function(element, serverType, accountManager) {
        this.element = element;
        this.loginButton = element.find('[login-button]');
        this.logoutButton = element.find('[logout-button]');
        this.avatar = element.find('[avatar]');
        this.serverType = serverType;
        this.accountManager = accountManager;

        if (this.accountManager.loadAccount()) {
            this.logoutButton.removeClass('hidden');
            this.avatar.removeClass('hidden');
            var accountId = this.accountManager.loadAccount().accountId;
            this.avatar.on('error', $.proxy(function() {
                this.avatar.attr('src', 'http://dummyimage.com/50x50.png');
            }, this));
            this.avatar.attr('src', 'http://dummyimage.com/100x50.png');
        } else {
            this.loginButton.removeClass('hidden');
        }

        this.loginButton.on('click', $.proxy(this.login, this));
        this.logoutButton.on('click', $.proxy(this.logout, this));
    };
    AccountController.prototype.logout = function() {
        this.accountManager.logout();
        location.reload(true);
        return false;
    };
    AccountController.prototype.login = function() {
        location.href = this.serverType.getUrl('/login?from=' + encodeURIComponent(location.href));
        return false;
    };


    var SignUpFormController = function(form, serverType, accountManager) {
        this.form = form;
        this.nameInput = this.form.find('#nameInput');
        this.emailInput = this.form.find('#emailInput');
        this.passwordInput = this.form.find('#passwordInput');
        this.confirmPasswordInput = this.form.find('#confirmPasswordInput');
        this.alert = this.form.find('.alert');
        this.serverType = serverType;
        this.accountManager = accountManager;

        this.form.on('submit', $.proxy(this.signUp, this));
    };

    SignUpFormController.prototype.signUp = function() {
        this.alert.addClass('hidden');
        this.accountManager.signUp(
            this.nameInput.val(),
            this.emailInput.val(),
            this.passwordInput.val(),
            this.confirmPasswordInput.val(),
            $.proxy(function(success) {
              if (success) {
                  //TODO show to check email then login later
                  location.href = this.serverType.getUrl('/login');
              } else {
                  this.alert.removeClass('hidden');
                  this.passwordInput.focus();
              }
            }, this));
        return false;
    };

    var LoginFormController = function(form, serverType, accountManager) {
        this.form = form;
        this.nameInput = this.form.find('#nameInput');
        this.passwordInput = this.form.find('#passwordInput');
        this.rememberMe = this.form.find('#rememberMeInput');
        this.alert = this.form.find('.alert');
        this.serverType = serverType;
        this.accountManager = accountManager;

        this.form.on('submit', $.proxy(this.login, this));
    };
    LoginFormController.prototype.getParameterByName = function (name) { /* http://stackoverflow.com/a/901144 */
        name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
        var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
            results = regex.exec(location.search);
        return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
    };
    LoginFormController.prototype.login = function() {
        this.alert.addClass('hidden');
        this.accountManager.login(this.nameInput.val(), this.passwordInput.val(), this.rememberMe.prop('checked'), $.proxy(function(success) {
            if (success) {
                var from = this.getParameterByName('from');
                location.href = from.length > 0 ? from : this.serverType.getUrl('/');
            } else {
                this.alert.removeClass('hidden');
                this.passwordInput.focus();
            }
        }, this));
        return false;
    };

    this.serverType = new ServerType(config.baseUrl, config.baseAccountUrl);
    this.accountService = new AccountService(this.serverType);
    this.accountManager = new AccountManager(this.accountService);

    $('[login-form-controller]').each($.proxy(function(index, element) {
        this.loginForm = new LoginFormController($(element), this.serverType, this.accountManager);
    }, this));

    $('[sign-up-form-controller]').each($.proxy(function(index, element) {
        this.signUpForm = new SignUpFormController($(element), this.serverType, this.accountManager);
    }, this));

    $('[show-if]').each($.proxy(function(index, element) {
        if (this.accountManager.loadAccount() && $(element).attr('show-if') == 'login' ||
            !this.accountManager.loadAccount() && $(element).attr('show-if') == 'logout') {
            $(element).removeClass('hidden');
        }
    }, this));

};

$(function() {
    KaifWeb.Config = {
        baseUrl: '',
        baseAccountUrl: '/api/account'
    };
    _kaifWeb = new KaifWeb(KaifWeb.Config);
});

var _kaifWeb = null;
