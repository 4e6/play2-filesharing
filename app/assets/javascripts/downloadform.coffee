root = exports ? this

root.checkPassword = ->
  url = $('#url').val()
  pass = $.trim $('#password').val()
  $.ajax
    url: '__checkPassword'
    type: 'post'
    data:
      'url': url
      'password': pass
    dataType: 'json'
    beforeSend: -> $('#passwordLoader').show()
    complete: -> $('#passwordLoader').hide()
    success: (j) ->
      if j.correct
        $('#passwordHint').html "correct"
        getFile(url, pass)
        # window.location.replace("http://localhost:9000")
        $('#download').attr("disabled", "disabled")
      else
        $('#passwordHint').html "try again"
    error: (j) -> $('#passwordHint').html j

getFile = (url, pass) ->
  $('#holder').attr("src", "__retrieve?url=#{url}&password=#{pass}")