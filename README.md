# Search Engine (Поисковый движок)

## Описание проекта
Этот проект представляет собой поисковый движок, разработанный с использованием **Java Spring **. Приложение индексирует веб-страницы, анализирует их содержимое и предоставляет функционал поиска по ключевым словам.

## Функциональность
- Представление статистики о проиндексированых сайтах и страницах. А также о количестве найденных лемм
- Индексация веб-страниц для списка сайтов предустановленных в application.yml в многопоточном режиме
- Индексация отдельной страницы
- Поиск по индексу с учетом частоты встречаемых слов
- REST API для работы с поиском

 ## Технологии
- **Java 17**  
- **Spring Boot**  
- **Spring Data JPA**  
- **Hibernate**  
- **MySQL**  
- **Lombok**  
- **Jsoup (для парсинга HTML)**

## Запуск проекта
1. Клонировать репозиторий:  
   ```bash
   git clone https://github.com/ywtor22/searchengine.git
   cd your-repo
2. Установить зависимости и собрать проект:
   ```bash
   mvn clean install
3. Настроить подключение к базе данных:
   логин / пароль в application.yml
   url для запуска в контейнере в application-prod.yml (предустановленный порт 3307)
   url для запуска из среды разработки в application-dev.yml (предустановленный порт 3306)
4. Запустить приложение в контейнере
- Убедится что запущен Docker
- Проверить что порты 8085 и 3306 свободные
- Перейти в консоли в папку с проектом
- выполнить команду
  ```bash
  docker-compose up -d --build
5. Для остановки контейнера выполнить команду в папке проекта
  ```bash
  docker-compose down
   
