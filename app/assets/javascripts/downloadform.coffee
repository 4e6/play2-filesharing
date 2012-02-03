root = exprts ? this

root.checkPassword = ->
  $.ajax
    url: 'checkPassword'
    type: 'post'
    data:
      'url': $('#url').val()
      'password': $.trim $('#password').val()
    dataType: 'json'
    beforeSend: -> $('#passwordLoader').show()
    complete: -> $('#passwordLoader').hide()
    success: (j) ->
      if j.correct
        $('#passwordHint').html "correct"
      else
        $('#passwordHint').html "try again"
