package com.ifpr.app_reuse.baseclasses

data class Coleta(
    val userId: String = "",
    val titulo: String = "",
    var endereco: String = "",
    var descricao: String = "",
    val imageUrl: String = "",
    var itens: List<ItemColeta>

)
