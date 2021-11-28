<!--
  - Licensed to the Apache Software Foundation (ASF) under one or more
  - contributor license agreements.  See the NOTICE file distributed with
  - this work for additional information regarding copyright ownership.
  - The ASF licenses this file to You under the Apache License, Version 2.0
  - (the "License"); you may not use this file except in compliance with
  - the License.  You may obtain a copy of the License at
  -
  -     http://www.apache.org/licenses/LICENSE-2.0
  -
  - Unless required by applicable law or agreed to in writing, software
  - distributed under the License is distributed on an "AS IS" BASIS,
  - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  - See the License for the specific language governing permissions and
  - limitations under the License.
  -->

<template>
  <el-row class="box-card">
    <div class="btn-group">
      <el-button
        :disabled="isGuest"
        class="btn-plus"
        type="primary"
        icon="el-icon-plus"
        @click="add"
      >{{ $t("dubboJob.btnTxt") }}
      </el-button>
    </div>
    <div class="table-wrap">
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column
          v-for="(item, index) in column"
          :key="index"
          :prop="item.prop"
          :label="item.label"
          :width="item.width"
        />
        <el-table-column :label="$t('dubboJob.table.operate')" fixed="right" width="200">
          <template slot-scope="scope">
            <el-tooltip
              :content="$t('dubboJob.table.operateDel')"
              class="item"
              effect="dark"
              placement="top"
            >
              <el-button
                :disabled="isGuest"
                size="small"
                type="danger"
                icon="el-icon-delete"
                @click="handlerDel(scope.row)"
              />
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination
          :total="total"
          :current-page="currentPage"
          background
          layout="prev, pager, next"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
    <el-dialog
      :title="$t('dubboJob.dubboJobDialog.title')"
      :visible.sync="addDialogVisible"
      width="1010px"
    >
      <el-form ref="form" :model="form" :rules="rules" label-width="170px">
        <el-form-item :label="$t('dubboJob.dubboJobDialog.name')" prop="name">
          <el-input :placeholder="$t('dubboJob.rules.name')" v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('dubboJob.dubboJobDialog.corn')" prop="corn">
          <el-input :placeholder="$t('dubboJob.rules.corn')" v-model="form.corn" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('dubboJob.dubboJobDialog.desc')">
          <el-input :placeholder="$t('dubboJob.rules.desc')" v-model="form.desc" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('dubboJob.dubboJobDialog.zkAddressList')" prop="zkAddressList">
          <el-input
            :placeholder="$t('dubboJob.rules.zkAddressList')"
            v-model="form.zkAddressList"
            autocomplete="off"
          />
        </el-form-item>
        <el-form-item :label="$t('dubboJob.dubboJobDialog.group')" prop="group">
          <el-input
            :placeholder="$t('dubboJob.rules.group')"
            v-model="form.group"
            autocomplete="off"
          />
        </el-form-item>
        <el-form-item :label="$t('dubboJob.dubboJobDialog.version')" prop="version">
          <el-input
            :placeholder="$t('dubboJob.rules.version')"
            v-model="form.version"
            autocomplete="off"
          />
        </el-form-item>
        <el-form-item :label="$t('dubboJob.dubboJobDialog.timeout')" prop="timeout">
          <el-input
            :placeholder="$t('dubboJob.rules.timeout')"
            v-model="form.timeout"
            autocomplete="off"
          />
        </el-form-item>
        <el-form-item :label="$t('dubboJob.dubboJobDialog.interfaceName')" prop="interfaceName">
          <el-input
            :placeholder="$t('dubboJob.rules.interfaceName')"
            v-model="form.interfaceName"
            autocomplete="off"
          />
        </el-form-item>
        <el-form-item :label="$t('dubboJob.dubboJobDialog.method')" prop="method">
          <el-input
            :placeholder="$t('dubboJob.rules.method')"
            v-model="form.method"
            autocomplete="off"
          />
        </el-form-item>
        <el-form-item :label="$t('dubboJob.dubboJobDialog.args')" prop="args">
          <el-input
            :placeholder="$t('dubboJob.rules.args')"
            v-model="form.args"
            autocomplete="off"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="addDialogVisible = false">{{ $t("dubboJob.dubboJobDialog.btnCancelTxt") }}</el-button>
        <el-tooltip class="item" effect="dark" :content="$t('dubboJob.dubboJobDialog.btnConnectTip')" placement="top">
          <el-button
            icon="el-icon-link"
            @click="handleConnect('form')">{{ $t("dubboJob.dubboJobDialog.btnConnectTxt") }}
          </el-button>
        </el-tooltip>
        <el-button type="primary" @click="onConfirm('form')">
          {{$t("dubboJob.dubboJobDialog.btnConfirmTxt")}}
        </el-button>
      </div>
    </el-dialog>
  </el-row>
</template>
<script>
import { mapActions } from 'vuex'
import clone from 'lodash/clone'
import API from '../api'

export default {
  name: 'DubboJob',
  data() {
    return {
      addDialogVisible: false,
      isGuest: window.localStorage.getItem('isGuest') === 'true',
      column: [
        {
          label: this.$t('dubboJob').dubboJobDialog.name,
          prop: 'name'
        },
        {
          label: this.$t('dubboJob').dubboJobDialog.corn,
          prop: 'corn'
        },
        {
          label: this.$t('dubboJob').dubboJobDialog.desc,
          prop: 'desc'
        },
        {
          label: this.$t('dubboJob').dubboJobDialog.zkAddressList,
          prop: 'zkAddressList'
        },
        {
          label: this.$t('dubboJob').dubboJobDialog.group,
          prop: 'group'
        },
        {
          label: this.$t('dubboJob').dubboJobDialog.version,
          prop: 'version'
        },
        {
          label: this.$t('dubboJob').dubboJobDialog.interfaceName,
          prop: 'interfaceName'
        },
        {
          label: this.$t('dubboJob').dubboJobDialog.method,
          prop: 'method'
        },
        {
          label: this.$t('dubboJob').dubboJobDialog.timeout,
          prop: 'timeout'
        },
        {
          label: this.$t('dubboJob').dubboJobDialog.args,
          prop: 'args'
        }
      ],
      form: {
        name: '',
        corn: '',
        desc: '',
        zkAddressList: '',
        group: '',
        interfaceName: '',
        method: '',
        timeout: 3000,
        args: ''
      },
      rules: {
        name: [
          {
            required: true,
            message: this.$t('dubboJob').rules.name,
            trigger: 'change'
          }
        ],
        corn: [
          {
            required: true,
            message: this.$t('dubboJob').rules.corn,
            trigger: 'change'
          }
        ],
        zkAddressList: [
          {
            required: true,
            message: this.$t('dubboJob').rules.zkAddressList,
            trigger: 'change'
          }
        ],
        group: [
          {
            required: false,
            message: this.$t('dubboJob').rules.group,
            trigger: 'change'
          }
        ],
        version: [
          {
            required: false,
            message: this.$t('dubboJob').rules.version,
            trigger: 'change'
          }
        ],
        interfaceName: [
          {
            required: true,
            message: this.$t('dubboJob').rules.interfaceName,
            trigger: 'change'
          }
        ],
        method: [
          {
            required: true,
            message: this.$t('dubboJob').rules.method,
            trigger: 'change'
          }
        ]
      },
      tableData: [],
      cloneTableData: [],
      currentPage: 1,
      pageSize: 10,
      total: null
    }
  },
  created() {
    this.getDubboJob()
  },
  methods: {
    ...mapActions(['setRegCenterActivated']),
    handleCurrentChange(val) {
      const data = clone(this.cloneTableData)
      this.tableData = data.splice(val - 1, this.pageSize)
    },
    getDubboJob() {
      API.getDubboJob().then(res => {
        const data = res.model
        this.total = data.length
        this.cloneTableData = clone(res.model)
        this.tableData = data.splice(0, this.pageSize)
      })
    },
    handleConnect(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          API.postDubboJobConnect(this.form).then(res => {
            this.addDialogVisible = true
            this.$notify({
              title: this.$t('common').notify.title,
              message: 'dubbo测试返回结果为：' + res.model,
              type: 'success'
            })
          })
        } else {
          return false
        }
      })
    },
    handlerDel(row) {
      const params = {
        name: row.name
      }
      API.deleteDubboJob(params).then(res => {
        this.$notify({
          title: this.$t('common').notify.title,
          message: this.$t('common').notify.delSucMessage,
          type: 'success'
        })
        this.getDubboJob()
      })
    },
    onConfirm(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          API.postDubboJob(this.form).then(res => {
            this.addDialogVisible = false
            this.$notify({
              title: this.$t('common').notify.title,
              message: this.$t('common').notify.addSucMessage,
              type: 'success'
            })
            this.getDubboJob()
          })
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    add() {
      this.addDialogVisible = true
    }
  }
}
</script>
<style lang='scss' scoped>
.btn-group {
  margin-bottom: 20px;
}

.pagination {
  float: right;
  margin: 10px -10px 10px 0;
}
</style>
