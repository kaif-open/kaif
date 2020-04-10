//please sync to messages.properties
library message_en;

import 'package:intl/intl.dart';
import 'package:intl/message_lookup_by_library.dart';

MessageLookupByLibrary get messages => new MessageLookup();

class MessageLookup extends MessageLookupByLibrary {

  get localeName => 'en';

  final messages = {

    "error.title": () => Intl.message("Your request cannot be processed"),
    "error.subtitle": () => Intl.message("Sorry, an error has occurred."),
    "error.status": () => Intl.message("Status:"),
    "error.message": () => Intl.message("Message:"),
    "success": () => Intl.message("Success"),
    "email.activation.title": () => Intl.message("kaif account activation"),
    "email.activation.greeting": (a0) => Intl.message("Dear ${a0}"),
    "email.activation.text1": () =>
        Intl.message(
            "Your kaif account has been created, please click on the URL below to activate it\:"),
    "email.activation.text2": () => Intl.message("Regards,"),
    "email.signature": () => Intl.message("kaif Team."),
    "email.reset-password.title": () =>
        Intl.message("kaif account password reset"),
    "email.reset-password.greeting": (a0) => Intl.message("Dear ${a0}"),
    "email.reset-password.text1": () =>
        Intl.message(
            "You have been requested password reset, please click on the URL below to reset it\:"),
    "email.reset-password.text2": () => Intl.message("Regards,"),
    "email.password-was-reset.title": () =>
        Intl.message("Your kaif password has been reset."),
    "email.password-was-reset.greeting": (a0) => Intl.message("Dear ${a0}"),
    "email.password-was-reset.text1": () =>
        Intl.message(
            "The password for your kaif id, has been successfully reset."),
    "email.password-was-reset.text2": (a0) =>
        Intl.message(
            "If you didn’t make this change or if you believe an unauthorized person has accessed your account, go to ${a0} to reset your password immediately."),
    "email.password-was-reset.text3": () => Intl.message("Regards,"),
    "rest-error.DataIntegrityViolationException": () =>
        Intl.message("Data corrupted"),
    "rest-error.DataAccessException": (a0) =>
        Intl.message("Could not access data\: ${a0}"),
    "rest-error.DuplicateKeyException": () =>
        Intl.message("Data already exist"),
    "rest-error.MethodArgumentNotValidException": (a0) =>
        Intl.message("Validation failed. ${a0}"),
    "rest-error.OptimisticLockingFailureException": () =>
        Intl.message("Data locked temporarily"),
    "rest-error.PermissionDeniedDataAccessException": () =>
        Intl.message("Permission denied"),
    "rest-error.PessimisticLockingFailureException": () =>
        Intl.message("Data locked"),
    "rest-error.QueryTimeoutException": () => Intl.message("Request timeout"),
    "rest-error.RuntimeException": (a0) =>
        Intl.message("Internal Server Error\: ${a0}"),
    "rest-error.Exception": (a0) =>
        Intl.message("Internal Server Error\: ${a0}"),
    "rest-error.EmptyResultDataAccessException": () =>
        Intl.message("Not found"),
    "rest-error.RestAccessDeniedException": () =>
        Intl.message(
            "Unauthorized access. Please make sure account activated and login again"),
    "sign-up.available": () => Intl.message("&\#10003; Available"),
    "sign-up.invalid-name": () => Intl.message("Invalid username"),
    "sign-up.name-already-taken": () => Intl.message("Already taken\!"),
    "sign-up.email-already-taken": () =>
        Intl.message("Email has been registered"),
    "sign-up.password-not-same": () =>
        Intl.message("Confirm password is not the same as password"),
    "account-menu.sign-in": () => Intl.message("Sign In"),
    "account-menu.sign-up": () => Intl.message("Sign Up"),
    "account-menu.sign-out": () => Intl.message("Sign Out"),
    "account-menu.debate-replies": () => Intl.message("New replies"),
    "account-menu.news-feed": () => Intl.message("News feed"),
    "account-menu.settings": () => Intl.message("Settings"),
    "account-settings.reactivation-sent": () =>
        Intl.message("Verification mail sent"),
    "account-settings.update-new-password-success": () =>
        Intl.message("Password update success"),
    "account-setting.update-description-success": () =>
        Intl.message("About me updated"),
    "account-setting.min-description": (a0) =>
        Intl.message("at least ${a0} characters"),
    "part-loader.permission-error": () =>
        Intl.message("Permission denied. Please sign in again and try again."),
    "article.min-title": (a0) =>
        Intl.message("Title at least ${a0} characters."),
    "article.min-content": (a0) =>
        Intl.message("Content at least ${a0} characters."),
    "article.create-success": () => Intl.message("Article created"),
    "article.url-exist": () => Intl.message("The url already shared."),
    "article.force-create": () => Intl.message("I don't care, just share this"),
    "debate.create-success": () => Intl.message("Comment created"),
    "debate.edit-success": () => Intl.message("Comment updated"),
    "debate.min-content": (a0) => Intl.message("at least ${a0} characters"),
    "debate.sign-in-to-debate": () =>
        Intl.message("Sign in first to join discussion"),
    "zone.invalid-zone": () => Intl.message("Invalid zone name"),
    "zone.available": () => Intl.message("&\#10003; Available"),
    "zone.zone-already-taken": () => Intl.message("Already taken\!"),
    "zone.create-success": () => Intl.message("Zone created"),
    "kmark.preview": () => Intl.message("Preview"),
    "kmark.finish-preview": () => Intl.message("Finish Preview"),
    "kmark.help": () => Intl.message("Format Help"),
    "kmark.finish-help": () => Intl.message("Close Help"),
    "kmark.auto-link-placeholder": () => Intl.message("Link"),
    "votable.sign-in-to-vote": () => Intl.message("Sign in first to vote"),
    "client-app-scope.public": () =>
        Intl.message("Read public articles and debates"),
    "client-app-scope.feed": () => Intl.message("Read news feed"),
    "client-app-scope.user": () => Intl.message("Basic user information"),
    "client-app-scope.article": () => Intl.message("Read and write articles"),
    "client-app-scope.debate": () => Intl.message("Read and write comments"),
    "client-app-scope.vote": () =>
        Intl.message("Vote on articles and comments"),
    "account.OldPasswordNotMatchException": () =>
        Intl.message("Old password is wrong"),
    "account.AuthenticateFailException": () =>
        Intl.message(
            "Authentication failed, please check name and password are correct."),
    "account.RequireCitizenException": () =>
        Intl.message(
            "You have not activated your account, please activate and try again."),
    "client-app.CallbackUriReservedException": () =>
        Intl.message("uri should not contains keyword: kaif"),
    "client-app.ClientAppNameReservedException": () =>
        Intl.message("App name should not use kaif"),
    "client-app.ClientAppMaxException": (a0) =>
        Intl.message("Only allow up to ${a0} apps"),
    "zone.CreditNotEnoughException": () =>
        Intl.message("You don't have enough credit to create a board."),
    "unit-test": () => Intl.message("testing")
  };
}