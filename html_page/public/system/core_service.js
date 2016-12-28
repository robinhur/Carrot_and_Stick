console.log("open core_service");

$.when(
    $.getScript( "./system/url_checker.js"),
    $.getScript( "./system/account_checker.js" ),
    $.getScript( "./system/page_config.js" ),
    $.Deferred(function( deferred ){
        $( deferred.resolve );
    })
).done(function(){

    $.getScript( "./system/html_loader.js" )

});