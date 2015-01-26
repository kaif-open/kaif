<#import "/spring.ftl" as spring />
<#import "macros/template.ftl" as template>

<@template.page {}>

<div class="header">
    <h1>Page Title</h1>

    <h2>A subtitle for your page goes here</h2>
</div>

<div class="content">
<h2 class="content-subhead">How to use this layout</h2>

<p>
    <@spring.messageText "foo-abc.def-xyz" "xxxDefault message" />

    To use this layout, you can just copy paste the HTML, along with the CSS in <a
        href="/css/layouts/side-menu.css" alt="Side Menu CSS">side-menu.css</a>, and the JavaScript
    in <a href="/js/ui.js">ui.js</a>. The JS file uses vanilla JavaScript to simply toggle an <code>active</code>
    class that makes the menu responsive.
</p>

<h2 class="content-subhead">Now Let's Speak Some Latin</h2>

<p>
    Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut
    labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco
    laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in
    voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat
    non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.
</p>

<h2 class="content-subhead">Try Resizing your Browser</h2>

<p>
    Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut
    labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco
    laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in
    voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat
    non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.
</p>
</div>

</@template.page>

<!--/\\\________/\\\______/\\\\\\\\\______/\\\\\\\\\\\___/\\\\\\\\\\\\\\\______/\\\\\\\\\__________________________/\\\_______
   _\/\\\_____/\\\//_____/\\\\\\\\\\\\\___\/////\\\///___\/\\\///////////_____/\\\\\\\\\\\\\_____________________/\\\//\\\_____
    _\/\\\__/\\\//_______/\\\/////////\\\______\/\\\______\/\\\_______________/\\\/////////\\\___________________/\\\_/\\\______
     _\/\\\\\\//\\\______\/\\\_______\/\\\______\/\\\______\/\\\\\\\\\\\______\/\\\_______\/\\\__________________\//\\\\//_______
      _\/\\\//_\//\\\_____\/\\\\\\\\\\\\\\\______\/\\\______\/\\\///////_______\/\\\\\\\\\\\\\\\_________________/\\\///\\\_______
       _\/\\\____\//\\\____\/\\\/////////\\\______\/\\\______\/\\\______________\/\\\/////////\\\_______________/\\\/__\///\\\/\\\_
        _\/\\\_____\//\\\___\/\\\_______\/\\\______\/\\\______\/\\\______________\/\\\_______\/\\\______________/\\\______\//\\\//__
         _\/\\\______\//\\\__\/\\\_______\/\\\___/\\\\\\\\\\\__\/\\\______________\/\\\_______\/\\\_____________\//\\\\\\\\\\\//\\\__
          _\///________\///___\///________\///___\///////////___\///_______________\///________\///_______________\///////////_\///___
  __/\\\________/\\\______/\\\\\\\\\______/\\\\\\\\\\\___/\\\\\\\\\\\\\\\______/\\\\\\\\\______/\\\\\_____/\\\______/\\\\\\\\\\\\_
   _\/\\\_____/\\\//_____/\\\\\\\\\\\\\___\/////\\\///___\/\\\///////////_____/\\\\\\\\\\\\\___\/\\\\\\___\/\\\____/\\\//////////__
    _\/\\\__/\\\//_______/\\\/////////\\\______\/\\\______\/\\\_______________/\\\/////////\\\__\/\\\/\\\__\/\\\___/\\\_____________
     _\/\\\\\\//\\\______\/\\\_______\/\\\______\/\\\______\/\\\\\\\\\\\______\/\\\_______\/\\\__\/\\\//\\\_\/\\\__\/\\\____/\\\\\\\_
      _\/\\\//_\//\\\_____\/\\\\\\\\\\\\\\\______\/\\\______\/\\\///////_______\/\\\\\\\\\\\\\\\__\/\\\\//\\\\/\\\__\/\\\___\/////\\\_
       _\/\\\____\//\\\____\/\\\/////////\\\______\/\\\______\/\\\______________\/\\\/////////\\\__\/\\\_\//\\\/\\\__\/\\\_______\/\\\_
        _\/\\\_____\//\\\___\/\\\_______\/\\\______\/\\\______\/\\\______________\/\\\_______\/\\\__\/\\\__\//\\\\\\__\/\\\_______\/\\\_
         _\/\\\______\//\\\__\/\\\_______\/\\\___/\\\\\\\\\\\__\/\\\______________\/\\\_______\/\\\__\/\\\___\//\\\\\__\//\\\\\\\\\\\\/__
          _\///________\///___\///________\///___\///////////___\///_______________\///________\///___\///_____\/////____\////////////_-->
