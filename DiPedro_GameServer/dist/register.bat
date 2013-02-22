@echo off
color 0C
title Register: Console
@java -Djava.util.logging.config.file=config/console.cfg -cp ./lib/*; com.dipedro.gameserver.register.GameServerRegister
@pause
