<html>
  <head>
    <title>New House</title>
  </head>
  <body>
    <div class="container">
      <form action = "/sample/house/create" method="POST">
        <div class="fieldset">
          <label>Name:</label><input type = "text" name="house.name"/>
        </div>
        <div class="fieldset">
        <label>Door width:</label><input type = "text" name="house.door[].width"/>
        </div>
        <div class="fieldset">
        <label>Door height:</label><input type = "text" name="house.door[].height"/>
        </div>
        <div class="fieldset">
          <input type='submit'/>
        </div>
      </form>
    </div>
  </body>
</html>