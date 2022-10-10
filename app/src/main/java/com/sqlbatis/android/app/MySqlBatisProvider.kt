package com.sqlbatis.android.app

import com.sqlbatis.android.SQLbatisHelper
import com.sqlbatis.android.provider.SQLbatisProvider

class MySqlBatisProvider: SQLbatisProvider() {

    override fun supportSqlHelper(): Class<out SQLbatisHelper> = MySqlBatisHelper::class.java

}