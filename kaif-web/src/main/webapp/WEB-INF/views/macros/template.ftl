<#import "/spring.ftl" as spring />

<#macro page config>

<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="A layout example with a side menu that hides on mobile, just like the Pure website.">

    <title>kaif.io</title>
    <link rel="stylesheet" href="css/pure-0.5/pure-min.css">
    <link rel="stylesheet" href="css/pure-0.5/layouts/side-menu.css">
</head>
<body>
<div id="layout">
    <!-- Menu toggle -->
    <a href="#menu" id="menuLink" class="menu-link">
        <!-- Hamburger icon -->
        <span></span>
    </a>

    <div id="menu">
        <div class="pure-menu pure-menu-open">
            <a class="pure-menu-heading" href="#">Company</a>

            <ul>
                <li><a href="#">Home</a></li>
                <li><a href="#">About</a></li>

                <li class="menu-item-divided pure-menu-selected">
                    <a href="#">Services</a>
                </li>

                <li><a href="#">Contact</a></li>
            </ul>
        </div>
    </div>

    <div id="main">
        <#nested>
    </div>
</div>
<script src="js/pure-0.5/ui.js"></script>

</body>
</html>

</#macro>

