Перед вами примитивный бот для ответа на реплики пользователей VK.
Реализация на Java, задействован фреймворк Spring, для небольшого сокращения кода использован Lombok.  
--- По условию задачи нельзя использовать библиотеки VkApi. ----

До начала работы требуется:
0) Создать/иметь группу в VK с правами доступа "администратор"
1) Получить необходимые параметры:
    - Получить access_token (Настройка -> Работа с API -> Создать ключ->Разрешаем все виды действий->Cоздать)
    - Получить group_id (Настройки -> Основная информация - Копируем id-группы)
    - Получить версию API (Работа с API -> Long Poll API), на момент написания 5.236

2)  Группа VK. Включаем Long Poll API -> Включено
 - Типы событий. Включаем "Входящее/Исходящее сообщение"

3) Группа VK. Настройки - Cообщения -> Включены - > Сохранить
    Настройки для бота -> Возможности ботов -> Включены -> Сохранить

4) Важно! Заполняем основные переменные в application.properties:
access_token={ВАШ ТОКЕН}
group_id={ID ВАШЕЙ ГРУППЫ}
ver_api={ВЕРСИЯ API}

Для старта приложения запускаем файл VkbotApplication.  

Команды: 

localhost:8080/start - Старт бота. Бот работает в фоновом режиме, отвечая только на сообщения пользователей в группе

localhost:8080/exit - Выключение бота

Результат:

Юзер: Привет

Бот: Вы писали: "Привет"
